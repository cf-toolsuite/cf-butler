package io.pivotal.cfapp.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

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
@Table("spaces")
public class Space {

    @Column("org_id")
    @JsonProperty("organization-id")
    private final String organizationId;

    @Column("org_name")
    @JsonProperty("organization-name")
    private final String organizationName;

    @Id
    @JsonProperty("space-id")
    private final String spaceId;

    @JsonProperty("space-name")
    private final String spaceName;


    @JsonCreator
    @PersistenceConstructor
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

}
