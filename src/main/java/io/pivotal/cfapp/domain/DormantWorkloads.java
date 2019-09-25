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
import lombok.ToString;


@Builder
@Getter
@EqualsAndHashCode
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

    public DormantWorkloads matchBySpace(List<Space> spaces) {
        List<AppDetail> matchingApps = new ArrayList<>();
        List<ServiceInstanceDetail> matchingServiceInstances = new ArrayList<>();
        for (Space s: spaces) {
            matchingApps.addAll(applications
                    .stream()
                        .filter(application ->
                            application.getOrganization().equalsIgnoreCase(s.getOrganization())
                                && application.getSpace().equalsIgnoreCase(s.getSpace()))
                        .collect(Collectors.toList()));
            matchingServiceInstances.addAll(serviceInstances
                    .stream()
                        .filter(serviceInstance ->
                            serviceInstance.getOrganization().equalsIgnoreCase(s.getOrganization())
                                && serviceInstance.getSpace().equalsIgnoreCase(s.getSpace()))
                        .collect(Collectors.toList()));
        }
        return DormantWorkloads.builder().applications(matchingApps).serviceInstances(matchingServiceInstances).build();
    }

    public String toString() {
        return String
                .format(
                    "Dormant Applications: [%s], Dormant Service Instances [%s]",
                    String.join(",", getApplications().stream().map(a -> a.getAppName()).collect(Collectors.toList())),
                    String.join(",", getServiceInstances().stream().map(s -> s.getName()).collect(Collectors.toList()))
                );
    }
}
