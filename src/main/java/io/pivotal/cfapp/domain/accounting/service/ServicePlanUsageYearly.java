package io.pivotal.cfapp.domain.accounting.service;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;

@Builder
@Getter
@JsonPropertyOrder({ "service_plan_name", "service_plan_guid", "year", "duration_in_hours", "maximum_instances", "average_instances"})
public class ServicePlanUsageYearly {

    @JsonProperty("service_plan_name")
    public String servicePlanName;

    @JsonProperty("service_plan_guid")
    public String servicePlanGuid;

    @JsonProperty("year")
    public Integer year;

    @Default
    @JsonProperty("duration_in_hours")
    public Double durationInHours = 0.0;

    @Default
    @JsonProperty("maximum_instances")
    public Integer maximumInstances = 0;

    @Default
    @JsonProperty("average_instances")
    public Double averageInstances = 0.0;

    @JsonCreator
    public ServicePlanUsageYearly(
        @JsonProperty("service_plan_name") String servicePlanName,
        @JsonProperty("service_plan_guid") String servicePlanGuid,
        @JsonProperty("year") Integer year,
        @JsonProperty("duration_in_hours") Double durationInHours,
        @JsonProperty("maximum_instances") Integer maximumInstances,
        @JsonProperty("average_instances") Double averageInstances) {
        this.servicePlanName = servicePlanName;
        this.servicePlanGuid = servicePlanGuid;
        this.year = year;
        this.durationInHours = durationInHours;
        this.maximumInstances = maximumInstances;
        this.averageInstances = averageInstances;
    }


}
