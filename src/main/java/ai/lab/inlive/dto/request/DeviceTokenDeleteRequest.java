package ai.lab.inlive.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Удаление FCM-токена устройства (логаут)")
public class DeviceTokenDeleteRequest {

    @NotBlank(message = "{validation.deviceToken.fcmToken.required}")
    @Schema(description = "FCM registration token устройства, с которого выполняется выход", example = "dG9rZW4...")
    private String fcmToken;
}
