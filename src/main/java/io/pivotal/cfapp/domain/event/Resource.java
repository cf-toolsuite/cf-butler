package io.pivotal.cfapp.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "entity",
    "metadata"
})
public class Resource {

    @JsonProperty("entity")
    private Entity entity;

    @JsonProperty("metadata")
    private ResourceMetadata metadata;

    @JsonCreator
    public Resource(
            @JsonProperty("entity") Entity entity,
            @JsonProperty("metadata") ResourceMetadata metadata
            ) {
        this.entity = entity;
        this.metadata = metadata;
    }

}
