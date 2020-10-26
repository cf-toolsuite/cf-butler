package io.pivotal.cfapp.domain;

import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.mapping.OutboundRow;
import org.springframework.r2dbc.core.Parameter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@WritingConverter
public class MessagePolicyWriteConverter implements Converter<MessagePolicy, OutboundRow> {

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public OutboundRow convert(MessagePolicy source) {
        OutboundRow row = new OutboundRow();
        row.put("id", Parameter.fromOrEmpty(source.getId(), String.class));
        row.put("owner_email_template", Parameter.fromOrEmpty(source.getOwnerTemplate() != null ? writeEmailNotificationTemplate(source.getOwnerTemplate()) : null, String.class));
        row.put("organization_whitelist", Parameter.fromOrEmpty(source.getOrganizationWhiteList().stream().filter(StringUtils::isNotBlank).collect(Collectors.joining(",")), String.class));
        return row;
    }

    private String writeEmailNotificationTemplate(EmailNotificationTemplate value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException jpe) {
            throw new RuntimeException("Problem writing email notification template", jpe);
        }
    }
}
