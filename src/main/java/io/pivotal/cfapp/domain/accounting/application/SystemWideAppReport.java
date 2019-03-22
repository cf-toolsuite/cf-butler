package io.pivotal.cfapp.domain.accounting.application;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@Data
@JsonPropertyOrder({"report_time", "monthly_reports", "yearly_reports"})
public class SystemWideAppReport {

    @JsonProperty("report_time")
    private String reportTime;

    @JsonProperty("monthly_reports")
    private List<MonthlyAppReport> monthlyReports;

    @JsonProperty("yearly_reports")
    private List<YearlyAppReport> yearlyReports;

}