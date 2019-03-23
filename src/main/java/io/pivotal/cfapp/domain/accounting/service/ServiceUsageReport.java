package io.pivotal.cfapp.domain.accounting.service;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@Data
@JsonPropertyOrder({"report_time", "monthly_service_reports", "yearly_service_report"})
public class ServiceUsageReport {

    @JsonProperty("report_time")
    public String reportTime;

    @JsonProperty("monthly_service_reports")
    public List<ServiceUsageMonthlyAggregate> monthlyServiceReports;

    @JsonProperty("yearly_service_report")
    public List<ServiceUsageYearlyAggregate> yearlyServiceReport;

}
