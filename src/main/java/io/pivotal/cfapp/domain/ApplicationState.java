package io.pivotal.cfapp.domain;

import java.util.Arrays;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonValue;

import org.springframework.util.Assert;

public enum ApplicationState {

    STARTED("started"),
    STOPPED("stopped");

    private String state;

    ApplicationState(String state) {
        this.state = state;
    }

    @JsonValue
    public String getState() {
        return state;
    }

    public static ApplicationState from(String state) {
        Assert.hasText(state, "ApplicationState must not be null or empty");
        ApplicationState result = Arrays.asList(ApplicationState.values()).stream().filter(s -> s.getState().equalsIgnoreCase(state)).collect(Collectors.toList()).get(0);
        Assert.notNull(result, String.format("Invalid ApplicationState, state=%s", state));
        return result;
    }
}