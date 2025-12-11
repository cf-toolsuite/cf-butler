package org.cftoolsuite.cfapp.domain;

import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.mapping.OutboundRow;
import org.springframework.r2dbc.core.Parameter;
import org.springframework.stereotype.Indexed;
import org.springframework.util.CollectionUtils;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Indexed
@WritingConverter
public class ServiceInstancePolicyWriteConverter implements Converter<ServiceInstancePolicy, OutboundRow> {

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public OutboundRow convert(ServiceInstancePolicy source) {
        OutboundRow row = new OutboundRow();
        row.put("id", Parameter.fromOrEmpty(source.getId(), String.class));
        row.put("git_commit", Parameter.fromOrEmpty(source.getGitCommit(), String.class));
        row.put("operation", Parameter.fromOrEmpty(source.getOperation(), String.class));
        row.put("description", Parameter.fromOrEmpty(source.getDescription(), String.class));
        row.put("options", Parameter.fromOrEmpty(CollectionUtils.isEmpty(source.getOptions()) ? null : writeOptions(source.getOptions()), String.class));
        row.put("organization_whitelist", Parameter.fromOrEmpty(source.getOrganizationWhiteList().stream().filter(StringUtils::isNotBlank).collect(Collectors.joining(",")), String.class));
        row.put("cron_expression", Parameter.fromOrEmpty(source.getCronExpression(), String.class));
        return row;
    }

    private String writeOptions(Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JacksonException jpe) {
            throw new RuntimeException("Problem writing options", jpe);
        }
    }
}
