package io.pivotal.cfapp.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import io.r2dbc.spi.Row;

public class Defaults {

    public static List<String> getColumnListOfStringValue(Row row, String columnName) {
        String csv = Defaults.getColumnValue(row, columnName, String.class);
        if (StringUtils.isNotBlank(csv)) {
            return Arrays.asList(csv.split("\\s*,\\s*"));
        } else {
            return new ArrayList<String>();
        }
    }

    public static <T> T getColumnValue(Row row, String columnName, Class<T> columnType) {
        try {
            return row.get(columnName, columnType);
        } catch (ClassCastException cce) {
            return null;
        }
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
        } catch (Exception cce) {
            return defaultValue;
        }
    }
}
