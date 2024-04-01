package org.cftoolsuite.cfapp.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.cftoolsuite.cfapp.domain.Stack;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class StacksCache {

    private final Map<String, Stack> stacksByName = new TreeMap<>();
    private final Map<String, Stack> stacksById = new HashMap<>();

    public boolean contains(String name) {
        return stacksByName.containsKey(name);
    }

    public Map<String, Stack> from(List<org.cloudfoundry.operations.stacks.Stack> input) {
        stacksByName.clear();
        stacksById.clear();
        input.forEach(
                s -> {
                    Stack stack = Stack.builder().id(s.getId()).name(s.getName()).description(s.getDescription()).build();
                    stacksByName.put(s.getName(), stack);
                    stacksById.put(s.getId(), stack);
                });
        return stacksByName;
    }

    public Stack getStackById(String id) {
        Assert.isTrue(StringUtils.isNotBlank(id), "Stack id must not be blank.");
        return stacksById.get(id);
    }

    public Stack getStackByName(String name) {
        Assert.isTrue(StringUtils.isNotBlank(name), "Stack name must not be blank.");
        return stacksByName.get(name);
    }
}
