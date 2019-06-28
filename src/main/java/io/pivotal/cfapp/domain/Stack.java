package io.pivotal.cfapp.domain;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Stack {

    private final String id;
    private final String name;
    private final String description;
}