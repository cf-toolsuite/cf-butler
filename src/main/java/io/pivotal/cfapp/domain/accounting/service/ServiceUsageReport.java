package io.pivotal.cfapp.domain.accounting.service;

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
@JsonPropertyOrder({"report_time", "monthly_service_reports", "yearly_service_report"})
public class ServiceUsageReport {

    @JsonProperty("report_time")
    public String reportTime;

    @Default
    @JsonProperty("monthly_service_reports")
    public List<ServiceUsageMonthlyAggregate> monthlyServiceReports = new ArrayList<>();

    @Default
    @JsonProperty("yearly_service_report")
    public List<ServiceUsageYearlyAggregate> yearlyServiceReport = new ArrayList<>();

    @JsonCreator
    public ServiceUsageReport(
        @JsonProperty("report_time") String reportTime,
        @JsonProperty("monthly_service_reports") List<ServiceUsageMonthlyAggregate> monthlyServiceReports,
        @JsonProperty("yearly_service_report") List<ServiceUsageYearlyAggregate> yearlyServiceReport) {
        this.reportTime = reportTime;
        this.monthlyServiceReports = monthlyServiceReports;
        this.yearlyServiceReport = yearlyServiceReport;
    }

}
