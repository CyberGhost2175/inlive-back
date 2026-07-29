package ai.lab.inlive.config;

import ai.lab.inlive.config.properties.FirebaseProperties;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(FirebaseProperties.class)
public class FirebaseConfig {

    private final FirebaseProperties firebaseProperties;

    @PostConstruct
    public void init() {
        if (!firebaseProperties.isEnabled()) {
            log.warn("Firebase is disabled via firebase.enabled=false — push notifications will be skipped");
            return;
        }

        if (!FirebaseApp.getApps().isEmpty()) {
            log.info("FirebaseApp already initialized");
            return;
        }

        String credentialsPath = firebaseProperties.getCredentialsPath();
        if (!StringUtils.hasText(credentialsPath)) {
            log.error("Firebase credentials path is empty — push notifications will be unavailable");
            return;
        }

        try (InputStream serviceAccount = openCredentials(credentialsPath)) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            FirebaseApp.initializeApp(options);
            log.info("FirebaseApp initialized successfully (credentials: {})", credentialsPath);
        } catch (Exception e) {
            log.error("Failed to initialize FirebaseApp — push notifications will be unavailable: {}",
                    e.getMessage(), e);
        }
    }

    private InputStream openCredentials(String credentialsPath) throws IOException {
        Path path = Path.of(credentialsPath);
        if (Files.exists(path)) {
            return new FileInputStream(path.toFile());
        }

        InputStream classpathStream = getClass().getClassLoader().getResourceAsStream(credentialsPath);
        if (classpathStream != null) {
            return classpathStream;
        }

        classpathStream = getClass().getClassLoader()
                .getResourceAsStream("firebase/" + Path.of(credentialsPath).getFileName());
        if (classpathStream != null) {
            return classpathStream;
        }

        throw new IOException("Firebase credentials file not found: " + credentialsPath);
    }
}
