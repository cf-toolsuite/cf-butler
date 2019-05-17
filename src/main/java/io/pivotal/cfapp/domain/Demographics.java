package io.pivotal.cfapp.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Builder;
import lombok.Getter;
import lombok.Builder.Default;

@Builder
@Getter
@JsonPropertyOrder({ "total-organizations", "total-spaces", "total-user-accounts", "total-service-accounts" })
public class Demographics {

    @Default
    @JsonProperty("total-organizations")
    private Long organizations = 0L;

    @Default
    @JsonProperty("total-spaces")
    private Long spaces = 0L;

    @Default
    @JsonProperty("total-user-accounts")
    private Long userAccounts = 0L;

    @Default
    @JsonProperty("total-service-accounts")
    private Long serviceAccounts = 0L;

    @JsonCreator
    public Demographics(
        @JsonProperty("total-organizations") Long organizations,
        @JsonProperty("total-spaces") Long spaces,
        @JsonProperty("total-user-accounts") Long userAccounts,
        @JsonProperty("total-service-accounts") Long serviceAccounts
    ) {
        this.organizations = organizations;
        this.spaces = spaces;
        this.userAccounts = userAccounts;
        this.serviceAccounts = serviceAccounts;
    }
}