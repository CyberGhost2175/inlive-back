package ai.lab.inlive.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "firebase")
public class FirebaseProperties {

    /**
     * Path to Firebase service account JSON (absolute or relative to working directory).
     */
    private String credentialsPath = "./uitap-com-firebase-adminsdk-fbsvc-0ce6fc1a67.json";

    /**
     * When false, FCM sends are skipped (tokens still stored).
     */
    private boolean enabled = true;
}
