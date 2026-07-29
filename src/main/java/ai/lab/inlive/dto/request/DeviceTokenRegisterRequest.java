package ai.lab.inlive.dto.request;

import ai.lab.inlive.entities.enums.DevicePlatform;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Регистрация/обновление FCM-токена устройства")
public class DeviceTokenRegisterRequest {

    @NotBlank(message = "{validation.deviceToken.fcmToken.required}")
    @Schema(description = "FCM registration token", example = "dG9rZW4...")
    private String fcmToken;

    @NotNull(message = "{validation.deviceToken.platform.required}")
    @Schema(description = "Платформа устройства", example = "android", allowableValues = {"ios", "android"})
    private DevicePlatform platform;
}
