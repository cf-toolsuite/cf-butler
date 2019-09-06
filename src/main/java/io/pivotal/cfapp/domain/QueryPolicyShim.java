package io.pivotal.cfapp.domain;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class QueryPolicyShim {

	private Long pk;
	private String id;
	private String description;
	private String queries;
	private String emailNotificationTemplate;

}