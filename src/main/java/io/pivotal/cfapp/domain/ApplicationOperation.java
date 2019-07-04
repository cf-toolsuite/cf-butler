package io.pivotal.cfapp.domain;

import java.util.Arrays;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonValue;

import org.springframework.util.Assert;

public enum ApplicationOperation {

    DELETE("delete"),
    SCALE("scale");

    private final String name;

    ApplicationOperation(String name) {
        this.name = name;
    }

    @JsonValue
    public String getName() {
        return name;
    }

    public static ApplicationOperation from(String name) {
        Assert.hasText(name, "ApplicationOperation must not be null or empty");
        ApplicationOperation result = Arrays.asList(ApplicationOperation.values()).stream().filter(s -> s.getName().equalsIgnoreCase(name)).collect(Collectors.toList()).get(0);
        Assert.notNull(result, String.format("Invalid ApplicationOperation, name=%s", name));
        return result;
    }
}