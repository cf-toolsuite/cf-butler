```java
package org.cftoolsuite.cfapp.domain.accounting.application;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

public record AppUsageYearly(
        @JsonProperty("year") Integer year,
        @JsonProperty("average_app_instances") Double averageAppInstances,
        @JsonProperty("maximum_app_instances") Integer maximumAppInstances,
        @JsonProperty("app_instance_hours") Double appInstanceHours) {

    @JsonCreator
    public AppUsageYearly {
    }

    public static class AppUsageYearlyBuilder {
        private Integer year;
        private Double averageAppInstances = 0.0;
        private Integer maximumAppInstances = 0;
        private Double appInstanceHours = 0.0;

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
}
```