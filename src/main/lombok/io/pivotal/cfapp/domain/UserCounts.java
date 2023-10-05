package io.pivotal.cfapp.domain;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
@JsonPropertyOrder({ "by-organization", "total-user-accounts", "total-service-accounts"})
public class UserCounts {

    @Default
    @JsonProperty("by-organization")
    private Map<String, Integer> byOrganization = new HashMap<>();

    @Default
    @JsonProperty("total-user-accounts")
    private Long totalUserAccounts = 0L;

    @Default
    @JsonProperty("total-service-accounts")
    private Long totalServiceAccounts = 0L;

    @JsonCreator
    public UserCounts(
            @JsonProperty("by-organization") Map<String, Integer> byOrganization,
            @JsonProperty("total-user-accounts") Long totalUserAccounts,
            @JsonProperty("total-service-accounts") Long totalServiceAccounts
            ) {
        this.byOrganization = byOrganization;
        this.totalUserAccounts = totalUserAccounts;
        this.totalServiceAccounts = totalServiceAccounts;
    }
}
