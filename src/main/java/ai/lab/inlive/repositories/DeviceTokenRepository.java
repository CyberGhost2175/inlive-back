package ai.lab.inlive.repositories;

import ai.lab.inlive.entities.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    Optional<DeviceToken> findByFcmToken(String fcmToken);

    Optional<DeviceToken> findByFcmTokenAndIsDeletedFalse(String fcmToken);

    List<DeviceToken> findAllByUserIdAndIsDeletedFalse(Long userId);

    List<DeviceToken> findAllByUserIdInAndIsDeletedFalse(Collection<Long> userIds);

    List<DeviceToken> findAllByFcmTokenInAndIsDeletedFalse(Collection<String> fcmTokens);
}
