package io.pivotal.cfapp.domain;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.r2dbc.spi.Row;

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
                .resourceWhiteList(source.get("resource_whitelist", String.class) != null ? new HashSet<String>(Arrays.asList(source.get("resource_whitelist", String.class).split("\\s*,\\s*"))): new HashSet<>())
                .resourceBlackList(source.get("resource_blacklist", String.class) != null ? new HashSet<String>(Arrays.asList(source.get("resource_blacklist", String.class).split("\\s*,\\s*"))): new HashSet<>())
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

