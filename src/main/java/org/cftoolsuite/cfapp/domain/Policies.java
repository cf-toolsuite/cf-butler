package org.cftoolsuite.cfapp.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({ "application-policies", "service-instance-policies", "endpoint-policies", "query-policies", "hygiene-policies", "legacy-policies","resource-notification-policies" })
public class Policies {

    @JsonProperty("application-policies")
    private List<ApplicationPolicy> applicationPolicies;

    @JsonProperty("service-instance-policies")
    private List<ServiceInstancePolicy> serviceInstancePolicies;

    @JsonProperty("endpoint-policies")
    private List<EndpointPolicy> endpointPolicies;

    @JsonProperty("query-policies")
    private List<QueryPolicy> queryPolicies;

    @JsonProperty("hygiene-policies")
    private List<HygienePolicy> hygienePolicies;

    @JsonProperty("legacy-policies")
    private List<LegacyPolicy> legacyPolicies;

    @JsonProperty("resource-notification-policies")
    private List<ResourceNotificationPolicy> resourceNotificationPolicies;

    @JsonCreator
    Policies(
            @JsonProperty("application-policies") List<ApplicationPolicy> applicationPolicies,
            @JsonProperty("service-instance-policies") List<ServiceInstancePolicy> serviceInstancePolicies,
            @JsonProperty("endpoint-policies") List<EndpointPolicy> endpointPolicies,
            @JsonProperty("query-policies") List<QueryPolicy> queryPolicies,
            @JsonProperty("hygiene-policies") List<HygienePolicy> hygienePolicies,
            @JsonProperty("legacy-policies") List<LegacyPolicy> legacyPolicies,
            @JsonProperty("resource-notification-policies") List<ResourceNotificationPolicy> resourceNotificationPolicies) {
        this.applicationPolicies = applicationPolicies;
        this.serviceInstancePolicies = serviceInstancePolicies;
        this.endpointPolicies = endpointPolicies;
        this.queryPolicies = queryPolicies;
        this.hygienePolicies = hygienePolicies;
        this.legacyPolicies = legacyPolicies;
        this.resourceNotificationPolicies = resourceNotificationPolicies;

    }

    public List<ApplicationPolicy> getApplicationPolicies() {
        return applicationPolicies != null ? applicationPolicies: Collections.emptyList();
    }

    public List<EndpointPolicy> getEndpointPolicies() {
        return endpointPolicies != null ? endpointPolicies: Collections.emptyList();
    }

    public List<HygienePolicy> getHygienePolicies() {
        return hygienePolicies != null ? hygienePolicies: Collections.emptyList();
    }

    public List<LegacyPolicy> getLegacyPolicies() {
        return legacyPolicies != null ? legacyPolicies: Collections.emptyList();
    }

    public List<QueryPolicy> getQueryPolicies() {
        return queryPolicies != null ? queryPolicies: Collections.emptyList();
    }

    public List<ServiceInstancePolicy> getServiceInstancePolicies() {
        return serviceInstancePolicies != null ? serviceInstancePolicies: Collections.emptyList();
    }

    public List<ResourceNotificationPolicy> getResourceNotificationPolicies() {
        return resourceNotificationPolicies != null ? resourceNotificationPolicies: Collections.emptyList();
    }

    public List<Policy> all() {
        List<Policy> policies = new ArrayList<>();
        policies.addAll(getApplicationPolicies());
        policies.addAll(getEndpointPolicies());
        policies.addAll(getHygienePolicies());
        policies.addAll(getLegacyPolicies());
        policies.addAll(getQueryPolicies());
        policies.addAll(getResourceNotificationPolicies());
        policies.addAll(getServiceInstancePolicies());
        return policies;
    }

    public Policy getById(String policyId) {
        return all()
                .stream()
                .filter(policy -> policy.getId().equals(policyId))
                .findFirst()
                .orElse(null);
    }

    @JsonIgnore
    public boolean isEmpty() {
        return getApplicationPolicies().isEmpty()
                && getServiceInstancePolicies().isEmpty()
                && getEndpointPolicies().isEmpty()
                && getQueryPolicies().isEmpty()
                && getHygienePolicies().isEmpty()
                && getLegacyPolicies().isEmpty()
                && getResourceNotificationPolicies().isEmpty();
    }

}
