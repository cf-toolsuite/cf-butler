package io.pivotal.cfapp.domain;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({ "application-policies", "service-instance-policies", "query-policies", "hygiene-policies" })
public class Policies {

	@JsonProperty("application-policies")
	private List<ApplicationPolicy> applicationPolicies;

	@JsonProperty("service-instance-policies")
	private List<ServiceInstancePolicy> serviceInstancePolicies;

	@JsonProperty("query-policies")
	private List<QueryPolicy> queryPolicies;

	@JsonProperty("hygiene-policies")
	private List<HygienePolicy> hygienePolicies;

	@JsonCreator
	public Policies(
			@JsonProperty("application-policies") List<ApplicationPolicy> applicationPolicies,
			@JsonProperty("service-instance-policies") List<ServiceInstancePolicy> serviceInstancePolicies,
			@JsonProperty("query-policies") List<QueryPolicy> queryPolicies,
			@JsonProperty("hygiene-policies") List<HygienePolicy> hygienePolicies) {
		this.applicationPolicies = applicationPolicies;
		this.serviceInstancePolicies = serviceInstancePolicies;
		this.queryPolicies = queryPolicies;
		this.hygienePolicies = hygienePolicies;
	}

	public List<ApplicationPolicy> getApplicationPolicies() {
		return applicationPolicies != null ? applicationPolicies: Collections.emptyList();
	}

	public List<ServiceInstancePolicy> getServiceInstancePolicies() {
		return serviceInstancePolicies != null ? serviceInstancePolicies: Collections.emptyList();
	}

	public List<QueryPolicy> getQueryPolicies() {
		return queryPolicies != null ? queryPolicies: Collections.emptyList();
	}

	public List<HygienePolicy> getHygienePolicies() {
		return hygienePolicies != null ? hygienePolicies: Collections.emptyList();
	}

	@JsonIgnore
	public boolean isEmpty() {
		return getApplicationPolicies().isEmpty()
				&& getServiceInstancePolicies().isEmpty()
				&& getQueryPolicies().isEmpty()
				&& getHygienePolicies().isEmpty();
	}

}
