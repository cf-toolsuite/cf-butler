package org.cftoolsuite.cfapp.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class Stack {

    private final String id;
    private final String name;
    private final String description;
}
