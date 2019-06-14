package io.pivotal.cfapp.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@JsonPropertyOrder({"organization", "space"})
@EqualsAndHashCode
@ToString
public class Space {

    @JsonProperty("organization")
    private String organization;

    @JsonProperty("space")
    private String space;

    @JsonCreator
    public Space(
        @JsonProperty("organization") String organization,
        @JsonProperty("space") String space) {
            this.organization = organization;
            this.space = space;
    }

    public String getOrganization() {
        return organization;
    }

    public String getSpace() {
        return space;
    }
}