package io.pivotal.cfapp.domain.product;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class OmInfo {

    @JsonProperty("info")
    private Info info;

    @JsonCreator
    public OmInfo(@JsonProperty("info") Info info) {
        this.info = info;
    }

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
}