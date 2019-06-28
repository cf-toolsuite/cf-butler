package io.pivotal.cfapp.repository;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Component;

import io.pivotal.cfapp.domain.Stack;

@Component
public class StacksCache {

    private final Map<String, Stack> stacks = new TreeMap<>();

    public Stack getStack(String id) {
        return stacks.get(id);
    }

    public Map<String, Stack> from(List<org.cloudfoundry.operations.stacks.Stack> input) {
        stacks.clear();
        input.forEach(
            s -> stacks.put(s.getId(),
                    Stack.builder().id(s.getId()).name(s.getName()).description(s.getDescription()).build()));
        return stacks;
    }
}