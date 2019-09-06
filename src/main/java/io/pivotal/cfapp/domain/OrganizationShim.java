package io.pivotal.cfapp.domain;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class OrganizationShim {

    private String id;
    private String orgName;

}