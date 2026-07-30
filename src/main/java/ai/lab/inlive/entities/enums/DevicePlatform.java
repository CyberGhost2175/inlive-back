package ai.lab.inlive.entities.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DevicePlatform {
    IOS("ios"),
    ANDROID("android"),
    WEB("web");

    private final String value;

    DevicePlatform(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static DevicePlatform fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (DevicePlatform platform : values()) {
            if (platform.value.equalsIgnoreCase(value) || platform.name().equalsIgnoreCase(value)) {
                return platform;
            }
        }
        throw new IllegalArgumentException("Unknown platform: " + value);
    }
}
