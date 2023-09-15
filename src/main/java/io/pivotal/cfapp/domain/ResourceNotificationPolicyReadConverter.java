package io.pivotal.cfapp.domain;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Indexed;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.pivotal.cfapp.util.CsvUtil;
import io.r2dbc.spi.Row;

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
                .resourceEmailTemplate(readEmailNotificationTemplate(source.get("resource_email_template", String.class) == null ? "{}": source.get("resource_email_template", String.class)))
                .resourceEmailMetadata(readResourceEmailMetadata(source.get("resource_email_metadata", String.class) == null ? "{}": source.get("resource_email_metadata", String.class)))
                .resourceWhiteList(CsvUtil.parse(source.get("resource_whitelist", String.class)))
                .resourceBlackList(CsvUtil.parse(source.get("resource_blacklist", String.class)))
                .build();
    }

    private EmailNotificationTemplate readEmailNotificationTemplate(String value) {
        try {
            return mapper.readValue(value, EmailNotificationTemplate.class);
        } catch (IOException ioe) {
            throw new RuntimeException("Problem reading email notification template", ioe);
        }
    }

    private ResourceEmailMetadata readResourceEmailMetadata(String value) {
        try {
            return mapper.readValue(value, ResourceEmailMetadata.class);
        } catch (IOException ioe) {
            throw new RuntimeException("Problem reading email notification template", ioe);
        }
    }


}

