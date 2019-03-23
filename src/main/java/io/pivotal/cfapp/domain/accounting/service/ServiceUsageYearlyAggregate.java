package io.pivotal.cfapp.domain.accounting.service;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@Data
@JsonPropertyOrder({"service_name", "service_guid", "year", "duration_in_hours", "maximum_instances", "average_instances", "plans"})
public class ServiceUsageYearlyAggregate {

    @JsonProperty("service_name")
    public String serviceName;

    @JsonProperty("service_guid")
    public String serviceGuid;

    @JsonProperty("year")
    public Integer year;

    @JsonProperty("duration_in_hours")
    public Double durationInHours;

    @JsonProperty("maximum_instances")
    public Integer maximumInstances;

    @JsonProperty("average_instances")
    public Integer averageInstances;

    @JsonProperty("plans")
    public List<ServicePlanUsageYearly> plans;

}
