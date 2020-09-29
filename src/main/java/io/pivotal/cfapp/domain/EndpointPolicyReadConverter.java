package io.pivotal.cfapp.domain;

import java.io.IOException;
import java.util.Set;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.r2dbc.spi.Row;

@ReadingConverter
public class EndpointPolicyReadConverter implements Converter<Row, EndpointPolicy> {

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public EndpointPolicy convert(Row source) {
        return
                EndpointPolicy
                .builder()
                .pk(source.get("pk", Long.class))
                .id(source.get("id", String.class))
                .description(source.get("description", String.class))
                .endpoints(readEndpoints(source.get("endpoints", String.class) == null ? "[]" : source.get("endpoints", String.class)))
                .emailNotificationTemplate(
                        readEmailNotificationTemplate(
                                source.get("email_notification_template", String.class) == null
                                ? "{}"
                                        : source.get("email_notification_template", String.class)))
                .build();
    }

    private EmailNotificationTemplate readEmailNotificationTemplate(String value) {
        try {
            return mapper.readValue(value, EmailNotificationTemplate.class);
        } catch (IOException ioe) {
            throw new RuntimeException("Problem reading email notification template", ioe);
        }
    }

    private Set<String> readEndpoints(String value) {
        try {
            return mapper.readValue(value, new TypeReference<Set<String>>() {});
        } catch (IOException ioe) {
            throw new RuntimeException("Problem reading endpoints", ioe);
        }
    }
}

