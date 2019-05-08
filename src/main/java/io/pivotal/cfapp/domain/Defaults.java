package io.pivotal.cfapp.domain;

public class Defaults {

    public static <T> T getValueOrDefault(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }
}