package io.pivotal.cfapp.service;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import io.pivotal.cfapp.domain.Stack;

@Component
public class StacksCache {

    private final Map<String, Stack> stacks = new TreeMap<>();

    public Stack getStack(String name) {
        Assert.isTrue(StringUtils.isNotBlank(name), "Stack name must not be blank.");
        return stacks.get(name);
    }

    public boolean contains(String name) {
        return getStack(name) != null ? true: false;
    }

    public Map<String, Stack> from(List<org.cloudfoundry.operations.stacks.Stack> input) {
        stacks.clear();
        input.forEach(
            s -> stacks.put(s.getId(),
                    Stack.builder().id(s.getId()).name(s.getName()).description(s.getDescription()).build()));
        return stacks;
    }
}