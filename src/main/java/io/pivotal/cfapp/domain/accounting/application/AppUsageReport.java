package io.pivotal.cfapp.domain.accounting.application;

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
@JsonPropertyOrder({"report_time", "monthly_reports", "yearly_reports"})
public class AppUsageReport {

    @JsonProperty("report_time")
    private String reportTime;

    @Default
    @JsonProperty("monthly_reports")
    private List<AppUsageMonthly> monthlyReports = new ArrayList<>();

    @Default
    @JsonProperty("yearly_reports")
    private List<AppUsageYearly> yearlyReports = new ArrayList<>();

    @JsonCreator
    public AppUsageReport(
        @JsonProperty("report_time") String reportTime,
        @JsonProperty("monthly_reports") List<AppUsageMonthly> monthlyReports,
        @JsonProperty("yearly_reports") List<AppUsageYearly> yearlyReports) {
        this.reportTime = reportTime;
        this.monthlyReports = monthlyReports;
        this.yearlyReports = yearlyReports;
    }

}