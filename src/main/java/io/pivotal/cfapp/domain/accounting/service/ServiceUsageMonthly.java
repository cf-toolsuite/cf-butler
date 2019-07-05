package io.pivotal.cfapp.domain.accounting.service;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Builder;
import lombok.Getter;
import lombok.Builder.Default;

@Builder
@Getter
@JsonPropertyOrder({"month", "year", "duration_in_hours", "average_instances", "maximum_instances"})
public class ServiceUsageMonthly {

    @JsonProperty("month")
    public Integer month;

    @JsonProperty("year")
    public Integer year;

    @Default
    @JsonProperty("duration_in_hours")
    public Double durationInHours = 0.0;

    @Default
    @JsonProperty("average_instances")
    public Double averageInstances = 0.0;

    @Default
    @JsonProperty("maximum_instances")
    public Integer maximumInstances = 0;

    @JsonCreator
    public ServiceUsageMonthly(
        @JsonProperty("month") Integer month,
        @JsonProperty("year") Integer year,
        @JsonProperty("duration_in_hours") Double durationInHours,
        @JsonProperty("average_instances") Double averageInstances,
        @JsonProperty("maximum_instances") Integer maximumInstances) {
        this.month = month;
        this.year = year;
        this.durationInHours = durationInHours;
        this.averageInstances = averageInstances;
        this.maximumInstances = maximumInstances;
    }

}
