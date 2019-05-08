package io.pivotal.cfapp.domain.accounting.task;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@Data
@JsonPropertyOrder({"report_time", "monthly_reports", "yearly_reports"})
public class TaskUsageReport {

    @JsonProperty("report_time")
    private String reportTime;

    @JsonProperty("monthly_reports")
    private List<TaskUsageMonthly> monthlyReports = new ArrayList<>();

    @JsonProperty("yearly_reports")
    private List<TaskUsageYearly> yearlyReports = new ArrayList<>();
}

