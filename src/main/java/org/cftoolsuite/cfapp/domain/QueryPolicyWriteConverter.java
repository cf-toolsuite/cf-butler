package org.cftoolsuite.cfapp.domain;

import java.util.Set;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.mapping.OutboundRow;
import org.springframework.r2dbc.core.Parameter;
import org.springframework.stereotype.Indexed;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Indexed
@WritingConverter
public class QueryPolicyWriteConverter implements Converter<QueryPolicy, OutboundRow> {

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public OutboundRow convert(QueryPolicy source) {
        OutboundRow row = new OutboundRow();
        row.put("id", Parameter.fromOrEmpty(source.getId(), String.class));
        row.put("description", Parameter.fromOrEmpty(source.getDescription(), String.class));
        row.put("queries", Parameter.fromOrEmpty(CollectionUtils.isEmpty(source.getQueries()) ? null : writeQueries(source.getQueries()), String.class));
        row.put("email_notification_template", Parameter.fromOrEmpty(source.getEmailNotificationTemplate() != null ? writeEmailNotificationTemplate(source.getEmailNotificationTemplate()) : null, String.class));
        return row;
    }

    private String writeEmailNotificationTemplate(EmailNotificationTemplate value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException jpe) {
            throw new RuntimeException("Problem writing email notification template", jpe);
        }
    }

    private String writeQueries(Set<Query> value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException jpe) {
            throw new RuntimeException("Problem writing queries", jpe);
        }
    }
}
