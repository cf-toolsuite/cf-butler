package io.pivotal.cfapp.domain.accounting.service;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;

@Builder
@Getter
@JsonPropertyOrder({"service_name", "service_guid", "year", "duration_in_hours", "maximum_instances", "average_instances", "plans"})
public class ServiceUsageYearlyAggregate {

    @JsonProperty("service_name")
    public String serviceName;

    @JsonProperty("service_guid")
    public String serviceGuid;

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

    @Default
    @JsonProperty("plans")
    public List<ServicePlanUsageYearly> plans = new ArrayList<>();

    @JsonCreator
    public ServiceUsageYearlyAggregate(
        @JsonProperty("service_name") String serviceName,
        @JsonProperty("service_guid") String serviceGuid,
        @JsonProperty("year") Integer year,
        @JsonProperty("duration_in_hours") Double durationInHours,
        @JsonProperty("maximum_instances") Integer maximumInstances,
        @JsonProperty("average_instances") Double averageInstances,
        @JsonProperty("plans") List<ServicePlanUsageYearly> plans) {
        this.serviceName = serviceName;
        this.serviceGuid = serviceGuid;
        this.year = year;
        this.durationInHours = durationInHours;
        this.maximumInstances = maximumInstances;
        this.averageInstances = averageInstances;
        this.plans = plans;
    }


}
