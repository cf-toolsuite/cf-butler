package org.cftoolsuite.cfapp.domain;

import java.util.Set;

import org.cftoolsuite.cfapp.domain.EndpointRequest;
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
        row.put("endpoint_requests", Parameter.fromOrEmpty(CollectionUtils.isEmpty(source.getEndpointRequests()) ? null : writeEndpointRequests(source.getEndpointRequests()), String.class));
        row.put("email_notification_template", Parameter.fromOrEmpty(source.getEmailNotificationTemplate() != null ? writeEmailNotificationTemplate(source.getEmailNotificationTemplate()) : null, String.class));
        row.put("cron_expression", Parameter.fromOrEmpty(source.getCronExpression(), String.class));
        return row;
    }

    private String writeEmailNotificationTemplate(EmailNotificationTemplate value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException jpe) {
            throw new RuntimeException("Problem writing email notification template", jpe);
        }
    }

    private String writeEndpointRequests(Set<EndpointRequest> value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException jpe) {
            throw new RuntimeException("Problem writing endpoint requests", jpe);
        }
    }
}
