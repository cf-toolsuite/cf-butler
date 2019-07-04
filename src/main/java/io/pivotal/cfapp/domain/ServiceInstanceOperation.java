package io.pivotal.cfapp.domain;

import java.util.Arrays;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonValue;

import org.springframework.util.Assert;

public enum ServiceInstanceOperation {

    DELETE("delete");

    private final String name;

    ServiceInstanceOperation(String name) {
        this.name = name;
    }

    @JsonValue
    public String getName() {
        return name;
    }

    public static ServiceInstanceOperation from(String name) {
        Assert.hasText(name, "ServiceInstanceOperation must not be null or empty");
        ServiceInstanceOperation result = Arrays.asList(ServiceInstanceOperation.values()).stream().filter(s -> s.getName().equalsIgnoreCase(name)).collect(Collectors.toList()).get(0);
        Assert.notNull(result, String.format("Invalid ServiceInstanceOperation, name=%s", name));
        return result;
    }
}