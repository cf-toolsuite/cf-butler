```java
package org.cftoolsuite.cfapp.domain.accounting.application;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"month", "year", "average_app_instances","maximum_app_instances", "app_instance_hours"})
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
        private Double averageAppInstances = 0.0;
        private Integer maximumAppInstances = 0;
        private Double appInstanceHours = 0.0;

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
    public AppUsageMonthly {
    }
}
```