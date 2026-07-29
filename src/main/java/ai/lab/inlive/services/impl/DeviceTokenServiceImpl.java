package ai.lab.inlive.services.impl;

import ai.lab.inlive.dto.request.DeviceTokenDeleteRequest;
import ai.lab.inlive.dto.request.DeviceTokenRegisterRequest;
import ai.lab.inlive.entities.DeviceToken;
import ai.lab.inlive.entities.User;
import ai.lab.inlive.exceptions.DbObjectNotFoundException;
import ai.lab.inlive.repositories.DeviceTokenRepository;
import ai.lab.inlive.repositories.UserRepository;
import ai.lab.inlive.services.DeviceTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceTokenServiceImpl implements DeviceTokenService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final UserRepository userRepository;
    private final MessageSource messageSource;

    @Override
    @Transactional
    public void registerOrUpdateToken(String keycloakId, DeviceTokenRegisterRequest request) {
        User user = userRepository.findByKeycloakIdAndIsDeletedFalse(keycloakId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND",
                        messageSource.getMessage("services.accommodation.userNotFound", new Object[]{keycloakId},
                                LocaleContextHolder.getLocale())));

        String token = request.getFcmToken().trim();
        DeviceToken deviceToken = deviceTokenRepository.findByFcmToken(token)
                .map(existing -> {
                    if (Boolean.TRUE.equals(existing.getIsDeleted())) {
                        deviceTokenRepository.delete(existing);
                        deviceTokenRepository.flush();
                        return new DeviceToken();
                    }
                    return existing;
                })
                .orElseGet(DeviceToken::new);

        deviceToken.setUser(user);
        deviceToken.setFcmToken(token);
        deviceToken.setPlatform(request.getPlatform());
        deviceTokenRepository.save(deviceToken);
        log.info("Registered/updated FCM token for user {} (platform={})", user.getId(), request.getPlatform());
    }

    @Override
    @Transactional
    public void deleteToken(String keycloakId, DeviceTokenDeleteRequest request) {
        User user = userRepository.findByKeycloakIdAndIsDeletedFalse(keycloakId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND",
                        messageSource.getMessage("services.accommodation.userNotFound", new Object[]{keycloakId},
                                LocaleContextHolder.getLocale())));

        String token = request.getFcmToken().trim();
        deviceTokenRepository.findByFcmToken(token)
                .filter(dt -> dt.getUser().getId().equals(user.getId()))
                .ifPresentOrElse(deviceToken -> {
                    deviceTokenRepository.delete(deviceToken);
                    log.info("Deleted FCM token for user {}", user.getId());
                }, () -> log.info("FCM token not found for user {} on logout — ignoring", user.getId()));
    }
}
