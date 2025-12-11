package org.cftoolsuite.cfapp.domain;

import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.mapping.OutboundRow;
import org.springframework.r2dbc.core.Parameter;
import org.springframework.stereotype.Indexed;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Indexed
@WritingConverter
public class LegacyPolicyWriteConverter implements Converter<LegacyPolicy, OutboundRow> {

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public OutboundRow convert(LegacyPolicy source) {
        OutboundRow row = new OutboundRow();
        row.put("id", Parameter.fromOrEmpty(source.getId(), String.class));
        row.put("git_commit", Parameter.fromOrEmpty(source.getGitCommit(), String.class));
        row.put("stacks", Parameter.fromOrEmpty(source.getStacks().stream().filter(StringUtils::isNotBlank).collect(Collectors.joining(",")), String.class));
        row.put("service_offerings", Parameter.fromOrEmpty(source.getServiceOfferings().stream().filter(StringUtils::isNotBlank).collect(Collectors.joining(",")), String.class));
        row.put("operator_email_template", Parameter.fromOrEmpty(source.getOperatorTemplate() != null ? writeEmailNotificationTemplate(source.getOperatorTemplate()) : null, String.class));
        row.put("notifyee_email_template", Parameter.fromOrEmpty(source.getNotifyeeTemplate() != null ? writeEmailNotificationTemplate(source.getNotifyeeTemplate()) : null, String.class));
        row.put("organization_whitelist", Parameter.fromOrEmpty(source.getOrganizationWhiteList().stream().filter(StringUtils::isNotBlank).collect(Collectors.joining(",")), String.class));
        row.put("cron_expression", Parameter.fromOrEmpty(source.getCronExpression(), String.class));
        return row;
    }

    private String writeEmailNotificationTemplate(EmailNotificationTemplate value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JacksonException jpe) {
            throw new RuntimeException("Problem writing email notification template", jpe);
        }
    }
}
