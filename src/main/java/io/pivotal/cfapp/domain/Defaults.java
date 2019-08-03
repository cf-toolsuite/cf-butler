package io.pivotal.cfapp.domain;

import io.r2dbc.spi.Row;

public class Defaults {

    public static <T> T getValueOrDefault(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

    public static <T> T getColumnValueOrDefault(Row row, String columnName, Class<T> columnType, T defaultValue) {
        try {
            T value = row.get(columnName, columnType);
            return value == null ? defaultValue : value;
        } catch (ClassCastException cce) {
            return defaultValue;
        }
    }

    public static Object getColumnValueOrDefault(Row row, String columnName, Object defaultValue) {
        try {
            Object value = row.get(columnName);
            return value == null ? defaultValue : value;
        } catch (ClassCastException cce) {
            return defaultValue;
        }
    }
}