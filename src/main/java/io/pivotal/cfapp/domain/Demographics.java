package io.pivotal.cfapp.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Builder;
import lombok.Getter;
import lombok.Builder.Default;

@Builder
@Getter
@JsonPropertyOrder({ "total-organizations", "total-spaces", "total-users" })
public class Demographics {

    @Default
    @JsonProperty("total-organizations")
    private Long organizations = 0L;

    @Default
    @JsonProperty("total-spaces")
    private Long spaces = 0L;

    @Default
    @JsonProperty("total-users")
    private Integer users = 0;

    @JsonCreator
    public Demographics(
        @JsonProperty("total-organizations") Long organizations,
        @JsonProperty("total-spaces") Long spaces,
        @JsonProperty("total-users") Integer users
    ) {
        this.organizations = organizations;
        this.spaces = spaces;
        this.users = users;
    }
}