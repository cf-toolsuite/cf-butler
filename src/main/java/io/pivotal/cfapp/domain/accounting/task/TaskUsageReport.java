package io.pivotal.cfapp.domain.accounting.task;

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
public class TaskUsageReport {

    @JsonProperty("report_time")
    private String reportTime;

    @Default
    @JsonProperty("monthly_reports")
    private List<TaskUsageMonthly> monthlyReports = new ArrayList<>();

    @Default
    @JsonProperty("yearly_reports")
    private List<TaskUsageYearly> yearlyReports = new ArrayList<>();

    @JsonCreator
    public TaskUsageReport(
        @JsonProperty("report_time") String reportTime,
        @JsonProperty("monthly_reports") List<TaskUsageMonthly> monthlyReports,
        @JsonProperty("yearly_reports") List<TaskUsageYearly> yearlyReports) {
        this.reportTime = reportTime;
        this.monthlyReports = monthlyReports;
        this.yearlyReports = yearlyReports;
    }

}

