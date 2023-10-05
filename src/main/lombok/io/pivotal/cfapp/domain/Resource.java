package io.pivotal.cfapp.domain;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@JsonPropertyOrder({ "guid","name","created_at", "updated_at", "metadata" })
public class Resource {

    @JsonProperty("guid")
    private String guid;

    @JsonProperty("name")
    private String name;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("updated_at")
    private Instant updatedAt;

    @JsonProperty("metadata")
    private EmbeddedMetadata metadata;

    @JsonCreator
    public Resource(

        @JsonProperty("guid") String guid,
        @JsonProperty("name") String name,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("updated_at") Instant updatedAt,
        @JsonProperty("metadata") EmbeddedMetadata metadata) {

        this.guid = guid;
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.metadata = metadata;
    }

}
