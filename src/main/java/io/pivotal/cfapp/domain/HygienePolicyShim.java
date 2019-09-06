package io.pivotal.cfapp.domain;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class HygienePolicyShim {

	private Long pk;
    private String id;
    private Integer daysSinceLastUpdate;
    private String operatorTemplate;
    private String notifyeeTemplate;
	private String organizationWhiteList;

}