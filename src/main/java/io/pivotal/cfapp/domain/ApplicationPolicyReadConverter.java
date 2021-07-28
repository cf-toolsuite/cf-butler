package io.pivotal.cfapp.domain;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Indexed;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.r2dbc.spi.Row;

@Indexed
@ReadingConverter
public class ApplicationPolicyReadConverter implements Converter<Row, ApplicationPolicy> {

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public ApplicationPolicy convert(Row source) {
        return
                ApplicationPolicy
                .builder()
                .pk(source.get("pk", Long.class))
                .id(source.get("id", String.class))
                .operation(source.get("operation", String.class))
                .description(source.get("description", String.class))
                .options(readOptions(source.get("options", String.class) == null ? "{}" : source.get("options", String.class)))
                .organizationWhiteList(source.get("organization_whitelist", String.class) != null ? new HashSet<String>(Arrays.asList(source.get("organization_whitelist", String.class).split("\\s*,\\s*"))): new HashSet<>())
                .state(source.get("state", String.class))
                .build();
    }

    private Map<String, Object> readOptions(String value) {
        try {
            return mapper.readValue(value, new TypeReference<Map<String, Object>>() {});
        } catch (IOException ioe) {
            throw new RuntimeException("Problem reading options", ioe);
        }
    }
}

