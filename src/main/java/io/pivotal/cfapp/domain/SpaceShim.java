package io.pivotal.cfapp.domain;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class SpaceShim {

    private String orgName;
    private String spaceName;

}