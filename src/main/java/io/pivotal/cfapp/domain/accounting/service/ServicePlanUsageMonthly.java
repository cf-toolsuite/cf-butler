package io.pivotal.cfapp.domain.accounting.service;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@Data
@JsonPropertyOrder({ "usages", "service_plan_name", "service_plan_guid"})
public class ServicePlanUsageMonthly {

    @JsonProperty("usages")
    public List<ServiceUsageMonthly> usages;

    @JsonProperty("service_plan_name")
    public String servicePlanName;

    @JsonProperty("service_plan_guid")
    public String servicePlanGuid;

}
