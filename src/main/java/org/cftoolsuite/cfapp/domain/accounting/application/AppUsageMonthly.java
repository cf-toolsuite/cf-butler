package org.cftoolsuite.cfapp.domain.accounting.application;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

public record AppUsageMonthly(
        @JsonProperty("month") Integer month,
        @JsonProperty("year") Integer year,
        @JsonProperty("average_app_instances") Double averageAppInstances,
        @JsonProperty("maximum_app_instances") Integer maximumAppInstances,
        @JsonProperty("app_instance_hours") Double appInstanceHours
) {

    public static class AppUsageMonthlyBuilder {
        private Integer month;
        private Integer year;
        private Double averageAppInstances;
        private Integer maximumAppInstances;
        private Double appInstanceHours;

        public AppUsageMonthlyBuilder() {
            this.averageAppInstances = 0.0;
            this.maximumAppInstances = 0;
            this.appInstanceHours = 0.0;
        }

        public AppUsageMonthlyBuilder month(Integer month) {
            this.month = month;
            return this;
        }

        public AppUsageMonthlyBuilder year(Integer year) {
            this.year = year;
            return this;
        }

        public AppUsageMonthlyBuilder averageAppInstances(Double averageAppInstances) {
            this.averageAppInstances = averageAppInstances;
            return this;
        }

        public AppUsageMonthlyBuilder maximumAppInstances(Integer maximumAppInstances) {
            this.maximumAppInstances = maximumAppInstances;
            return this;
        }

        public AppUsageMonthlyBuilder appInstanceHours(Double appInstanceHours) {
            this.appInstanceHours = appInstanceHours;
            return this;
        }

        public AppUsageMonthly build() {
            return new AppUsageMonthly(month, year, averageAppInstances, maximumAppInstances, appInstanceHours);
        }
    }

    @JsonCreator
    public AppUsageMonthly(
            @JsonProperty("month") Integer month,
            @JsonProperty("year") Integer year,
            @JsonProperty("average_app_instances") Double averageAppInstances,
            @JsonProperty("maximum_app_instances") Integer maximumAppInstances,
            @JsonProperty("app_instance_hours") Double appInstanceHours) {
        this.month = month;
        this.year = year;
        this.averageAppInstances = averageAppInstances;
        this.maximumAppInstances = maximumAppInstances;
        this.appInstanceHours = appInstanceHours;
    }

}