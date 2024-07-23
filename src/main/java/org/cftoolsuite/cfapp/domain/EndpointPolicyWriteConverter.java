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
public class EndpointPolicyWriteConverter implements Converter<EndpointPolicy, OutboundRow> {

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public OutboundRow convert(EndpointPolicy source) {
        OutboundRow row = new OutboundRow();
        row.put("id", Parameter.fromOrEmpty(source.getId(), String.class));
        row.put("git_commit", Parameter.fromOrEmpty(source.getGitCommit(), String.class));
        row.put("description", Parameter.fromOrEmpty(source.getDescription(), String.class));
        row.put("endpoints", Parameter.fromOrEmpty(CollectionUtils.isEmpty(source.getEndpoints()) ? null : writeEndpoints(source.getEndpoints()), String.class));
        row.put("email_notification_template", Parameter.fromOrEmpty(source.getEmailNotificationTemplate() != null ? writeEmailNotificationTemplate(source.getEmailNotificationTemplate()) : null, String.class));
        row.put("cron_expression", Parameter.fromOrEmpty(source.getCronExpression(), String.class));
        row.put("apply_json_to_csv_converter", Parameter.fromOrEmpty(source.isApplyJsonToCsvConverter(), Boolean.class));
        return row;
    }

    private String writeEmailNotificationTemplate(EmailNotificationTemplate value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException jpe) {
            throw new RuntimeException("Problem writing email notification template", jpe);
        }
    }

    private String writeEndpoints(Set<String> value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException jpe) {
            throw new RuntimeException("Problem writing endpoints", jpe);
        }
    }
}
