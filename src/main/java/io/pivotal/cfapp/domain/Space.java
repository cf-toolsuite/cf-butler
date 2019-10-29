package io.pivotal.cfapp.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@JsonPropertyOrder({ "organization-id", "organization-name", "space-id", "space-name" })
@EqualsAndHashCode
@ToString
public class Space {

    @JsonProperty("organization-id")
    private final String organizationId;

    @JsonProperty("organization-name")
    private final String organizationName;

    @JsonProperty("space-id")
    private final String spaceId;

    @JsonProperty("space-name")
    private final String spaceName;


    @JsonCreator
    Space(
        @JsonProperty("organization-id") String organizationId,
        @JsonProperty("organization-name") String organizationName,
        @JsonProperty("space-id") String spaceId,
        @JsonProperty("space-name") String spaceName) {
            this.organizationId = organizationId;
            this.organizationName = organizationName;
            this.spaceId = spaceId;
            this.spaceName = spaceName;
    }

    public static String tableName() {
        return "spaces";
    }

    public static String[] columnNames() {
        return new String[] { "org_id", "org_name", "space_id", "space_name" };
    }

}