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
public class QueryPolicyReadConverter implements Converter<Row, QueryPolicy> {

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public QueryPolicy convert(Row source) {
        return
                QueryPolicy
                .builder()
                .pk(source.get("pk", Long.class))
                .id(source.get("id", String.class))
                .gitCommit(source.get("git_commit", String.class))
                .description(source.get("description", String.class))
                .queries(readQueries(source.get("queries", String.class) == null ? "[]" : source.get("queries", String.class)))
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

    private Set<Query> readQueries(String value) {
        try {
            return mapper.readValue(value, new TypeReference<Set<Query>>() {});
        } catch (JacksonException je) {
            throw new RuntimeException("Problem reading queries", je);
        }
    }
}
