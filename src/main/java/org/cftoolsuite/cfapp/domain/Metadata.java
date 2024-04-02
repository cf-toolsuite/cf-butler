package org.cftoolsuite.cfapp.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Metadata {

    @JsonProperty("metadata")
    private EmbeddedMetadata metadata;

    @JsonCreator
    public Metadata(
            @JsonProperty("metadata") EmbeddedMetadata metadata
            ) {
        this.metadata = metadata;
    }

    @JsonIgnore
    public boolean isValid() {
        if (metadata == null) {
            return true;
        } else {
            return metadata.isValid();
        }


    }
}
