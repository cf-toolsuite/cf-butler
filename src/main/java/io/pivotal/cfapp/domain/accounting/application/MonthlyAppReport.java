package io.pivotal.cfapp.domain.accounting.application;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@Data
@JsonPropertyOrder({"month", "year", "average_app_instances","maximum_app_instances", "app_instance_hours"})
public class MonthlyAppReport {

    @JsonProperty("month")
    private Integer month;

    @JsonProperty("year")
    private Integer year;

    @JsonProperty("average_app_instances")
    private Double averageAppInstances;

    @JsonProperty("maximum_app_instances")
    private Integer maximumAppInstances;

    @JsonProperty("app_instance_hours")
    private Double appInstanceHours;

}