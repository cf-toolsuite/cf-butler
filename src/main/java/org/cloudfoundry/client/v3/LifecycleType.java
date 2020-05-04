package org.cloudfoundry.client.v3;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum LifecycleType {

    BUILDPACK("buildpack"),

    DOCKER("docker"),

    KPACK("kpack");

    private final String value;

    LifecycleType(String value) {
        this.value = value;
    }

    @JsonCreator
    public static LifecycleType from(String s) {
        switch (s.toLowerCase()) {
            case "buildpack":
                return BUILDPACK;
            case "docker":
                return DOCKER;
            case "kpack":
                return KPACK;
            default:
                throw new IllegalArgumentException(String.format("Unknown lifecycle type: %s", s));
        }
    }

    @JsonValue
    public String getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return getValue();
    }

}
