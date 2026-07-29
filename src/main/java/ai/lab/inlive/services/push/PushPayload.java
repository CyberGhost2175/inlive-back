package ai.lab.inlive.services.push;

import ai.lab.inlive.entities.enums.PushEntityType;
import ai.lab.inlive.entities.enums.PushNotificationType;
import lombok.Builder;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Builder
public class PushPayload {

    private final PushNotificationType type;
    private final String entityId;
    private final PushEntityType entityType;
    private final String newStatus;
    private final String title;
    private final String body;

    public Map<String, String> toDataMap() {
        Map<String, String> data = new HashMap<>();
        data.put("type", type.name());
        data.put("entityId", entityId);
        data.put("entityType", entityType.name());
        data.put("newStatus", newStatus != null ? newStatus : "");
        data.put("title", title);
        data.put("body", body);
        return data;
    }
}
