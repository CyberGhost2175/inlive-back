package ai.lab.inlive.services.impl;

import ai.lab.inlive.config.properties.FirebaseProperties;
import ai.lab.inlive.entities.DeviceToken;
import ai.lab.inlive.repositories.DeviceTokenRepository;
import ai.lab.inlive.services.PushNotificationService;
import ai.lab.inlive.services.push.PushPayload;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.SendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushNotificationServiceImpl implements PushNotificationService {

    private static final int FCM_MULTICAST_LIMIT = 500;

    private final DeviceTokenRepository deviceTokenRepository;
    private final DeviceTokenCleanupService deviceTokenCleanupService;
    private final FirebaseProperties firebaseProperties;

    @Override
    public void sendToUser(Long userId, PushPayload payload) {
        if (userId == null || payload == null) {
            return;
        }
        sendToUsers(List.of(userId), payload);
    }

    @Override
    public void sendToUsers(Collection<Long> userIds, PushPayload payload) {
        if (CollectionUtils.isEmpty(userIds) || payload == null) {
            return;
        }

        Set<Long> distinctUserIds = userIds.stream()
                .filter(id -> id != null)
                .collect(Collectors.toCollection(HashSet::new));

        if (distinctUserIds.isEmpty()) {
            return;
        }

        Runnable sendAction = () -> doSend(distinctUserIds, payload);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sendAction.run();
                }
            });
        } else {
            sendAction.run();
        }
    }

    private void doSend(Set<Long> userIds, PushPayload payload) {
        if (!firebaseProperties.isEnabled() || FirebaseApp.getApps().isEmpty()) {
            log.debug("FCM skipped (disabled or FirebaseApp not initialized). type={}, users={}",
                    payload.getType(), userIds);
            return;
        }

        try {
            List<DeviceToken> deviceTokens = deviceTokenRepository.findAllByUserIdInAndIsDeletedFalse(userIds);
            if (deviceTokens.isEmpty()) {
                log.debug("No FCM tokens for users {} (type={})", userIds, payload.getType());
                return;
            }

            List<String> tokens = deviceTokens.stream()
                    .map(DeviceToken::getFcmToken)
                    .distinct()
                    .toList();

            log.info("Sending FCM data message type={} entityId={} to {} token(s) for user(s) {}",
                    payload.getType(), payload.getEntityId(), tokens.size(), userIds);

            for (int i = 0; i < tokens.size(); i += FCM_MULTICAST_LIMIT) {
                List<String> batch = tokens.subList(i, Math.min(i + FCM_MULTICAST_LIMIT, tokens.size()));
                sendBatch(batch, payload);
            }
        } catch (Exception e) {
            log.error("Unexpected error while sending FCM (type={}, entityId={}): {}",
                    payload.getType(), payload.getEntityId(), e.getMessage(), e);
        }
    }

    private void sendBatch(List<String> tokens, PushPayload payload) {
        try {
            MulticastMessage message = MulticastMessage.builder()
                    .putAllData(payload.toDataMap())
                    .addAllTokens(tokens)
                    .build();

            var response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
            List<String> invalidTokens = new ArrayList<>();

            List<SendResponse> responses = response.getResponses();
            for (int i = 0; i < responses.size(); i++) {
                SendResponse sendResponse = responses.get(i);
                if (sendResponse.isSuccessful()) {
                    continue;
                }

                String token = tokens.get(i);
                FirebaseMessagingException exception = sendResponse.getException();
                MessagingErrorCode errorCode = exception != null ? exception.getMessagingErrorCode() : null;

                log.warn("FCM send failed for token suffix=...{}: errorCode={}, message={}",
                        tokenSuffix(token),
                        errorCode,
                        exception != null ? exception.getMessage() : "unknown");

                if (errorCode == MessagingErrorCode.UNREGISTERED
                        || errorCode == MessagingErrorCode.INVALID_ARGUMENT) {
                    invalidTokens.add(token);
                }
            }

            if (!invalidTokens.isEmpty()) {
                try {
                    deviceTokenCleanupService.deleteByTokens(invalidTokens);
                } catch (Exception e) {
                    log.error("Failed to remove invalid FCM tokens: {}", e.getMessage(), e);
                }
            }

            log.info("FCM batch done: success={}, failure={}, invalidRemoved={}",
                    response.getSuccessCount(), response.getFailureCount(), invalidTokens.size());
        } catch (Exception e) {
            log.error("FCM batch send failed (type={}, entityId={}): {}",
                    payload.getType(), payload.getEntityId(), e.getMessage(), e);
        }
    }

    private static String tokenSuffix(String token) {
        if (token == null || token.length() < 8) {
            return "****";
        }
        return token.substring(token.length() - 8);
    }
}
