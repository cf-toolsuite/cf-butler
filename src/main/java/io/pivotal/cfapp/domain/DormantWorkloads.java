package io.pivotal.cfapp.domain;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@EqualsAndHashCode
@ToString
@JsonPropertyOrder({ "applications", "service-instances" })
public class DormantWorkloads {

    @Default
    @JsonProperty("applications")
    private List<AppDetail> applications = new ArrayList<>();

    @Default
    @JsonProperty("service-instances")
    private List<ServiceInstanceDetail> serviceInstances = new ArrayList<>();

    @JsonCreator
    public DormantWorkloads(
        @JsonProperty("applications") List<AppDetail> applications,
        @JsonProperty("service-instances") List<ServiceInstanceDetail> serviceInstances
    ) {
        this.applications = applications;
        this.serviceInstances = serviceInstances;
    }
}