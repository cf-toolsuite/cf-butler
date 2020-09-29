package io.pivotal.cfapp.domain;

import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.mapping.OutboundRow;
import org.springframework.r2dbc.core.Parameter;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@WritingConverter
public class ApplicationPolicyWriteConverter implements Converter<ApplicationPolicy, OutboundRow> {

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public OutboundRow convert(ApplicationPolicy source) {
        OutboundRow row = new OutboundRow();
        row.put("id", Parameter.fromOrEmpty(source.getId(), String.class));
        row.put("operation", Parameter.fromOrEmpty(source.getOperation(), String.class));
        row.put("description", Parameter.fromOrEmpty(source.getDescription(), String.class));
        row.put("state", Parameter.fromOrEmpty(source.getState(), String.class));
        row.put("organization_whitelist", Parameter.fromOrEmpty(source.getOrganizationWhiteList().stream().filter(StringUtils::isNotBlank).collect(Collectors.joining(",")), String.class));
        row.put("options", Parameter.fromOrEmpty(CollectionUtils.isEmpty(source.getOptions()) ? null : writeOptions(source.getOptions()), String.class));
        return row;
    }

    private String writeOptions(Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException jpe) {
            throw new RuntimeException("Problem writing options", jpe);
        }
    }
}
