package io.pivotal.cfapp.domain;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class LegacyPolicyShim {

	private Long pk;
    private String id;
    private String stacks;
    private String serviceOfferings;
    private String operatorEmailTemplate;
    private String notifyeeEmailTemplate;
	private String organizationWhitelist;

}