package org.cftoolsuite.cfapp.domain;

import java.util.Set;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Indexed;

import io.r2dbc.spi.Row;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Indexed
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
                .endpointRequests(readEndpointRequests(source.get("endpoint_requests", String.class) == null ? "[]" : source.get("endpoint_requests", String.class)))
                .emailNotificationTemplate(
                        readEmailNotificationTemplate(
                                source.get("email_notification_template", String.class) == null
                                ? "{}"
                                        : source.get("email_notification_template", String.class)))
                .cronExpression(source.get("cron_expression", String.class))
                .build();
    }

    private EmailNotificationTemplate readEmailNotificationTemplate(String value) {
        try {
            return mapper.readValue(value, EmailNotificationTemplate.class);
        } catch (JacksonException je) {
            throw new RuntimeException("Problem reading email notification template", je);
        }
    }

    private Set<EndpointRequest> readEndpointRequests(String value) {
        try {
            return mapper.readValue(value, new TypeReference<Set<EndpointRequest>>() {});
        } catch (JacksonException je) {
            throw new RuntimeException("Problem reading endpoint requests", je);
        }
    }
}
