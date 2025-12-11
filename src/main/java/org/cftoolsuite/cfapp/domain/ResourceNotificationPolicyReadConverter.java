package org.cftoolsuite.cfapp.domain;


import org.cftoolsuite.cfapp.util.CsvUtil;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Indexed;

import io.r2dbc.spi.Row;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Indexed
@ReadingConverter
public class ResourceNotificationPolicyReadConverter implements Converter<Row, ResourceNotificationPolicy> {

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public ResourceNotificationPolicy convert(Row source) {
        return
                ResourceNotificationPolicy
                .builder()
                .pk(source.get("pk", Long.class))
                .id(source.get("id", String.class))
                .gitCommit(source.get("git_commit", String.class))
                .resourceEmailTemplate(readEmailNotificationTemplate(source.get("resource_email_template", String.class) == null ? "{}": source.get("resource_email_template", String.class)))
                .resourceEmailMetadata(readResourceEmailMetadata(source.get("resource_email_metadata", String.class) == null ? "{}": source.get("resource_email_metadata", String.class)))
                .resourceWhiteList(CsvUtil.parse(source.get("resource_whitelist", String.class)))
                .resourceBlackList(CsvUtil.parse(source.get("resource_blacklist", String.class)))
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

    private ResourceEmailMetadata readResourceEmailMetadata(String value) {
        try {
            return mapper.readValue(value, ResourceEmailMetadata.class);
        } catch (JacksonException je) {
            throw new RuntimeException("Problem reading email notification template", je);
        }
    }


}
