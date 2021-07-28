package io.pivotal.cfapp.domain.product;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class OmInfo {

    @Builder
    @Getter
    public static class Info {

        @JsonProperty("version")
        private String version;

        @JsonCreator
        public Info(@JsonProperty("version") String version) {
            this.version = version;
        }
    }

    @JsonProperty("info")
    private Info info;

    @JsonCreator
    public OmInfo(@JsonProperty("info") Info info) {
        this.info = info;
    }

    public Double getMajorMinorVersion() {
        String[] versionParts = info.getVersion().split("v");
        String[] buildParts = versionParts[0].split("-");
        int lastDot = buildParts[0].lastIndexOf(".");
        Double result;
        if (lastDot > 1) {
            result = Double.valueOf(buildParts[0].substring(0, lastDot));
        } else {
            result = Double.valueOf(buildParts[0]);
        }
        return result;
    }
}
