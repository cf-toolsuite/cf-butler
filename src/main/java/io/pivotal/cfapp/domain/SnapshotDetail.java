package io.pivotal.cfapp.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@AllArgsConstructor(access=AccessLevel.PACKAGE)
@NoArgsConstructor(access=AccessLevel.PACKAGE)
@Getter
@ToString
@JsonPropertyOrder({ "applications", "service-instances", "application-relationships", "user-accounts", "service-accounts" })
public class SnapshotDetail {

    @Default
    @JsonProperty("applications")
    private List<AppDetail> applications = new ArrayList<>();

    @Default
    @JsonProperty("service-instances")
    private List<ServiceInstanceDetail> serviceInstances = new ArrayList<>();

    @Default
    @JsonProperty("application-relationships")
    private List<AppRelationship> applicationRelationships = new ArrayList<>();

    @Default
    @JsonProperty("user-accounts")
    private Set<String> userAccounts = new HashSet<>();

    @Default
    @JsonProperty("service-accounts")
    private Set<String> serviceAccounts = new HashSet<>();

}
