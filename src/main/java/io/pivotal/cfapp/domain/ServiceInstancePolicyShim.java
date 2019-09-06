package io.pivotal.cfapp.domain;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ServiceInstancePolicyShim {

	private Long pk;
	private String id;
	private String operation;
	private String description;
	private String options;
	private String organizationWhitelist;

}