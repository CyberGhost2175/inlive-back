package ai.lab.inlive.entities;

import ai.lab.inlive.entities.enums.DevicePlatform;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

@BatchSize(size = 50)
@Getter
@Setter
@Entity
@RequiredArgsConstructor
@Table(
        name = "device_tokens",
        uniqueConstraints = @UniqueConstraint(name = "uk_device_tokens_fcm_token", columnNames = "fcm_token"),
        indexes = {
                @Index(name = "idx_device_tokens_user_id", columnList = "user_id")
        }
)
public class DeviceToken extends AbstractEntity<Long> {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "fcm_token", nullable = false, length = 512)
    private String fcmToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private DevicePlatform platform;
}
