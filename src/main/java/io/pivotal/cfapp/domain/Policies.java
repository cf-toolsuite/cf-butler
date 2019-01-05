package io.pivotal.cfapp.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Getter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "application-policies", "service-instance-policies"})
@Getter
public class Policies {


	@JsonProperty("application-policies")
	private List<ApplicationPolicy> applicationPolicies;
	
	@JsonProperty("service-instance-policies")
	private List<ServiceInstancePolicy> serviceInstancePolicies;
	
	
	@JsonCreator
	public Policies(@JsonProperty("application-policies") List<ApplicationPolicy> applicationPolicies, 
			@JsonProperty("service-instance-policies") List<ServiceInstancePolicy> serviceInstancePolicies) {
		this.applicationPolicies = applicationPolicies;
		this.serviceInstancePolicies = serviceInstancePolicies;
	}
	
}
