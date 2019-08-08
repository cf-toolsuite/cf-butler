package io.pivotal.cfapp.domain.event;

import java.time.Instant;

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
"created_at",
"guid",
"updated_at",
"url"
})
public class ResourceMetadata {

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("guid")
    private String guid;

    @JsonProperty("updated_at")
    private Instant updatedAt;

    @JsonProperty("url")
    private String url;

    @JsonCreator
    public ResourceMetadata(
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("guid") String guid,
        @JsonProperty("updated_at") Instant updatedAt,
        @JsonProperty("url") String url) {
        this.createdAt = createdAt;
        this.guid = guid;
        this.updatedAt = updatedAt;
        this.url = url;
    }

}