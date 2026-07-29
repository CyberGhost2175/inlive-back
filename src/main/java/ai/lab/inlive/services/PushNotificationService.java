package ai.lab.inlive.services;

import ai.lab.inlive.services.push.PushPayload;

import java.util.Collection;

public interface PushNotificationService {

    void sendToUser(Long userId, PushPayload payload);

    void sendToUsers(Collection<Long> userIds, PushPayload payload);
}
