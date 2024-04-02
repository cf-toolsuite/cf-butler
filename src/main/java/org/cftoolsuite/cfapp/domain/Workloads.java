package org.cftoolsuite.cfapp.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.Getter;


@Builder
@Getter
@EqualsAndHashCode
@JsonPropertyOrder({ "applications", "service-instances", "application-relationships" })
public class Workloads {

    @Default
    @JsonProperty("applications")
    private List<AppDetail> applications = new ArrayList<>();

    @Default
    @JsonProperty("service-instances")
    private List<ServiceInstanceDetail> serviceInstances = new ArrayList<>();

    @Default
    @JsonProperty("application-relationships")
    private List<AppRelationship> appRelationships = new ArrayList<>();

    @JsonCreator
    public Workloads(
            @JsonProperty("applications") List<AppDetail> applications,
            @JsonProperty("service-instances") List<ServiceInstanceDetail> serviceInstances,
            @JsonProperty("application-relationships") List<AppRelationship> appRelationships
            ) {
        this.applications = applications;
        this.serviceInstances = serviceInstances;
        this.appRelationships = appRelationships;
    }

    public Workloads matchBySpace(List<Space> spaces) {
        List<AppDetail> matchingApps = new ArrayList<>();
        List<ServiceInstanceDetail> matchingServiceInstances = new ArrayList<>();
        List<AppRelationship> matchingAppRelationships = new ArrayList<>();
        for (Space s: spaces) {
            matchingApps.addAll(applications
                    .stream()
                    .filter(application ->
                    application.getOrganization().equalsIgnoreCase(s.getOrganizationName())
                    && application.getSpace().equalsIgnoreCase(s.getSpaceName()))
                    .collect(Collectors.toList()));
            matchingServiceInstances.addAll(serviceInstances
                    .stream()
                    .filter(serviceInstance ->
                    serviceInstance.getOrganization().equalsIgnoreCase(s.getOrganizationName())
                    && serviceInstance.getSpace().equalsIgnoreCase(s.getSpaceName()))
                    .collect(Collectors.toList()));
            matchingAppRelationships.addAll(appRelationships
                    .stream()
                    .filter(appRelationship ->
                    appRelationship.getOrganization().equalsIgnoreCase(s.getOrganizationName())
                    && appRelationship.getSpace().equalsIgnoreCase(s.getSpaceName()))
                    .collect(Collectors.toList()));
        }
        return Workloads.builder().applications(matchingApps).serviceInstances(matchingServiceInstances).appRelationships(matchingAppRelationships).build();
    }

    @Override
    public String toString() {
        return String
                .format(
                        "Workloads comprised of... \n\tApplications: [%s],\n\tService Instances [%s],\n\tApplication Relationships [%s]",
                        String.join(",", getApplications().stream().map(AppDetail::getAppName).collect(Collectors.toList())),
                        String.join(",", getServiceInstances().stream().map(ServiceInstanceDetail::getName).collect(Collectors.toList())),
                        String.join(",", getAppRelationships().stream().map(AppRelationship::getAppName).collect(Collectors.toList()))
                        );
    }
}
