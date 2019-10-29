package io.pivotal.cfapp.domain;

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
@JsonPropertyOrder({ "applications", "service-instances" })
public class Workloads {

    @Default
    @JsonProperty("applications")
    private List<AppDetail> applications = new ArrayList<>();

    @Default
    @JsonProperty("service-instances")
    private List<ServiceInstanceDetail> serviceInstances = new ArrayList<>();

    @JsonCreator
    public Workloads(
        @JsonProperty("applications") List<AppDetail> applications,
        @JsonProperty("service-instances") List<ServiceInstanceDetail> serviceInstances
    ) {
        this.applications = applications;
        this.serviceInstances = serviceInstances;
    }

    public Workloads matchBySpace(List<Space> spaces) {
        List<AppDetail> matchingApps = new ArrayList<>();
        List<ServiceInstanceDetail> matchingServiceInstances = new ArrayList<>();
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
        }
        return Workloads.builder().applications(matchingApps).serviceInstances(matchingServiceInstances).build();
    }

    public String toString() {
        return String
                .format(
                    "Workloads comprised of... \n\tApplications: [%s],\n\tService Instances [%s]",
                    String.join(",", getApplications().stream().map(a -> a.getAppName()).collect(Collectors.toList())),
                    String.join(",", getServiceInstances().stream().map(s -> s.getName()).collect(Collectors.toList()))
                );
    }
}
