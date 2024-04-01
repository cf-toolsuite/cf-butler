package org.cftoolsuite.cfapp.domain.event;

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
    "actee",
    "actee_name",
    "actee_type",
    "actor",
    "actor_name",
    "actor_type",
    "actor_username",
    "metadata",
    "organization_guid",
    "space_guid",
    "timestamp",
    "type"
})
public class Entity {

    @JsonProperty("actee")
    private String actee;

    @JsonProperty("actee_name")
    private String acteeName;

    @JsonProperty("actee_type")
    private String acteeType;

    @JsonProperty("actor")
    private String actor;

    @JsonProperty("actor_name")
    private String actorName;

    @JsonProperty("actor_type")
    private String actorType;

    @JsonProperty("actor_username")
    private String actorUsername;

    @JsonProperty("metadata")
    private EntityMetadata metadata;

    @JsonProperty("organization_guid")
    private String organizationGuid;

    @JsonProperty("space_guid")
    private String spaceGuid;

    @JsonProperty("timestamp")
    private Instant timestamp;

    @JsonProperty("type")
    private String type;

    @JsonCreator
    public Entity(
            @JsonProperty("actee") String actee,
            @JsonProperty("actee_name") String acteeName,
            @JsonProperty("actee_type") String acteeType,
            @JsonProperty("actor") String actor,
            @JsonProperty("actor_name") String actorName,
            @JsonProperty("actor_type") String actorType,
            @JsonProperty("actor_username") String actorUsername,
            @JsonProperty("metadata") EntityMetadata metadata,
            @JsonProperty("organization_guid") String organizationGuid,
            @JsonProperty("space_guid") String spaceGuid,
            @JsonProperty("timestamp") Instant timestamp,
            @JsonProperty("type") String type
            ) {
        this.actee = actee;
        this.acteeName = acteeName;
        this.acteeType = acteeType;
        this.actor = actor;
        this.actorType = actorType;
        this.actorUsername = actorUsername;
        this.metadata = metadata;
        this.organizationGuid = organizationGuid;
        this.spaceGuid = spaceGuid;
        this.timestamp = timestamp;
        this.type = type;
    }
}
