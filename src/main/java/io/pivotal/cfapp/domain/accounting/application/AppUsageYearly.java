package io.pivotal.cfapp.domain.accounting.application;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Builder;
import lombok.Getter;
import lombok.Builder.Default;

@Builder
@Getter
@JsonPropertyOrder({"year", "average_app_instances", "maximum_app_instances", "app_instance_hours"})
public class AppUsageYearly {

    @JsonProperty("year")
    private Integer year;

    @Default
    @JsonProperty("average_app_instances")
    private Double averageAppInstances = 0.0;

    @Default
    @JsonProperty("maximum_app_instances")
    private Integer maximumAppInstances = 0;

    @Default
    @JsonProperty("app_instance_hours")
    private Double appInstanceHours = 0.0;

    @JsonCreator
    public AppUsageYearly(
        @JsonProperty("year") Integer year,
        @JsonProperty("average_app_instances") Double averageAppInstances,
        @JsonProperty("maximum_app_instances") Integer maximumAppInstances,
        @JsonProperty("app_instance_hours") Double appInstanceHours) {
        this.year = year;
        this.averageAppInstances = averageAppInstances;
        this.maximumAppInstances = maximumAppInstances;
        this.appInstanceHours = appInstanceHours;
    }

}
