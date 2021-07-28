package io.pivotal.cfapp.domain;

import java.io.IOException;
import java.util.Set;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Indexed;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.r2dbc.spi.Row;

@Indexed
@ReadingConverter
public class QueryPolicyReadConverter implements Converter<Row, QueryPolicy> {

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public QueryPolicy convert(Row source) {
        return
                QueryPolicy
                .builder()
                .pk(source.get("pk", Long.class))
                .id(source.get("id", String.class))
                .description(source.get("description", String.class))
                .queries(readQueries(source.get("queries", String.class) == null ? "[]" : source.get("queries", String.class)))
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

    private Set<Query> readQueries(String value) {
        try {
            return mapper.readValue(value, new TypeReference<Set<Query>>() {});
        } catch (IOException ioe) {
            throw new RuntimeException("Problem reading queries", ioe);
        }
    }
}

