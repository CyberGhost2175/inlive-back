package ai.lab.inlive.services.impl;

import ai.lab.inlive.entities.DeviceToken;
import ai.lab.inlive.repositories.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceTokenCleanupService {

    private final DeviceTokenRepository deviceTokenRepository;

    @Transactional
    public void deleteByTokens(List<String> invalidTokens) {
        if (invalidTokens == null || invalidTokens.isEmpty()) {
            return;
        }

        Map<Long, DeviceToken> toDelete = new LinkedHashMap<>();
        for (DeviceToken token : deviceTokenRepository.findAllByFcmTokenInAndIsDeletedFalse(invalidTokens)) {
            toDelete.put(token.getId(), token);
        }
        for (String raw : invalidTokens) {
            deviceTokenRepository.findByFcmToken(raw).ifPresent(t -> toDelete.put(t.getId(), t));
        }

        if (!toDelete.isEmpty()) {
            deviceTokenRepository.deleteAll(new ArrayList<>(toDelete.values()));
            log.info("Removed {} invalid/unregistered FCM token(s)", toDelete.size());
        }
    }
}
