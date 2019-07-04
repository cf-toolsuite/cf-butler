package io.pivotal.cfapp.domain;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonValue;

import org.springframework.util.Assert;

public enum ApplicationState {

    STARTED("started"),
    STOPPED("stopped");

    private String name;

    ApplicationState(String name) {
        this.name = name;
    }

    @JsonValue
    public String getName() {
        return name;
    }

    public static ApplicationState from(String name) {
        Assert.hasText(name, "ApplicationState must not be null or empty");
        List<ApplicationState> states = Arrays.asList(ApplicationState.values());
        ApplicationState result = states.stream().filter(s -> s.getName().equalsIgnoreCase(name)).collect(Collectors.toList()).get(0);
        Assert.notNull(result, String.format("Invalid ApplicationState, name=%s", name));
        return result;
    }
}