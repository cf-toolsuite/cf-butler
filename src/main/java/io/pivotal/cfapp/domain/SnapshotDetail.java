package io.pivotal.cfapp.domain;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@AllArgsConstructor(access=AccessLevel.PACKAGE)
@NoArgsConstructor(access=AccessLevel.PACKAGE)
@Getter
@ToString
@JsonPropertyOrder({ "applications", "service-instances", "application-relationships", "users"})
public class SnapshotDetail {

    @JsonProperty("applications")
    private List<AppDetail> applications;

    @JsonProperty("service-instances")
    private List<ServiceInstanceDetail> serviceInstances;

    @JsonProperty("application-relationships")
    private List<AppRelationship> applicationRelationships;

    @JsonProperty("users")
    private Set<String> users;

}