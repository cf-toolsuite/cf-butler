```java
package org.cftoolsuite.cfapp.domain.accounting.application;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"report_time", "monthly_reports", "yearly_reports"})
public record AppUsageReport(
        @JsonProperty("report_time") String reportTime,
        @JsonProperty("monthly_reports") List<AppUsageMonthly> monthlyReports,
        @JsonProperty("yearly_reports") List<AppUsageYearly> yearlyReports
) {

    public AppUsageReport {
        if (monthlyReports == null) {
            monthlyReports = new ArrayList<>();
        }
        if (yearlyReports == null) {
            yearlyReports = new ArrayList<>();
        }
    }

    public static class AppUsageReportBuilder {
        private String reportTime;
        private List<AppUsageMonthly> monthlyReports;
        private List<AppUsageYearly> yearlyReports;

        public AppUsageReportBuilder reportTime(String reportTime) {
            this.reportTime = reportTime;
            return this;
        }

        public AppUsageReportBuilder monthlyReports(List<AppUsageMonthly> monthlyReports) {
            this.monthlyReports = monthlyReports;
            return this;
        }

        public AppUsageReportBuilder yearlyReports(List<AppUsageYearly> yearlyReports) {
            this.yearlyReports = yearlyReports;
            return this;
        }

        public AppUsageReport build() {
            return new AppUsageReport(reportTime, monthlyReports, yearlyReports);
        }
    }

    @JsonCreator
    public static AppUsageReportBuilder builder() {
        return new AppUsageReportBuilder();
    }
}
```