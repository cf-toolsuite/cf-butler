package io.pivotal.cfapp.domain;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class EndpointPolicyShim {

	private Long pk;
	private String id;
	private String description;
	private String endpoints;
	private String emailNotificationTemplate;

}