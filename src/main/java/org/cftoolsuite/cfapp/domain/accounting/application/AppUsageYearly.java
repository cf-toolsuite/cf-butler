package org.cftoolsuite.cfapp.domain.accounting.application;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

public record AppUsageYearly(
    @JsonProperty("year")
    Integer year,
    @JsonProperty("average_app_instances")
    Double averageAppInstances,
    @JsonProperty("maximum_app_instances")
    Integer maximumAppInstances,
    @JsonProperty("app_instance_hours")
    Double appInstanceHours
) {

    @JsonCreator
    public AppUsageYearly(
            @JsonProperty("year") Integer year,
            @JsonProperty("average_app_instances") Double averageAppInstances,
            @JsonProperty("maximum_app_instances") Integer maximumAppInstances,
            @JsonProperty("app_instance_hours") Double appInstanceHours) {
        this(year, averageAppInstances == null ? 0.0 : averageAppInstances, maximumAppInstances == null ? 0 : maximumAppInstances, appInstanceHours == null ? 0.0 : appInstanceHours);
    }

    public static class AppUsageYearlyBuilder {
        private Integer year;
        private Double averageAppInstances;
        private Integer maximumAppInstances;
        private Double appInstanceHours;

        public AppUsageYearlyBuilder year(Integer year) {
            this.year = year;
            return this;
        }

        public AppUsageYearlyBuilder averageAppInstances(Double averageAppInstances) {
            this.averageAppInstances = averageAppInstances;
            return this;
        }

        public AppUsageYearlyBuilder maximumAppInstances(Integer maximumAppInstances) {
            this.maximumAppInstances = maximumAppInstances;
            return this;
        }

        public AppUsageYearlyBuilder appInstanceHours(Double appInstanceHours) {
            this.appInstanceHours = appInstanceHours;
            return this;
        }

        public AppUsageYearly build() {
            return new AppUsageYearly(year, averageAppInstances, maximumAppInstances, appInstanceHours);
        }
    }

    public static AppUsageYearlyBuilder builder() {
        return new AppUsageYearlyBuilder();
    }
}