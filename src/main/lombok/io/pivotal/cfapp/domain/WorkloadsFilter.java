package io.pivotal.cfapp.domain;

import java.util.Set;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class WorkloadsFilter {

    private Set<String> stacks;
    private Set<String> serviceOfferings;

}
