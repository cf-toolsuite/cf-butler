package io.pivotal.cfapp.domain;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.r2dbc.spi.Row;

@ReadingConverter
public class MessagePolicyReadConverter implements Converter<Row, MessagePolicy> {

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public MessagePolicy convert(Row source) {
        return
                MessagePolicy
                .builder()
                .pk(source.get("pk", Long.class))
                .id(source.get("id", String.class))
                .ownerTemplate(readEmailNotificationTemplate(source.get("owner_email_template", String.class) == null ? "{}": source.get("owner_email_template", String.class)))
                .organizationWhiteList(source.get("organization_whitelist", String.class) != null ? new HashSet<String>(Arrays.asList(source.get("organization_whitelist", String.class).split("\\s*,\\s*"))): new HashSet<>())
                .build();
    }

    private EmailNotificationTemplate readEmailNotificationTemplate(String value) {
        try {
            return mapper.readValue(value, EmailNotificationTemplate.class);
        } catch (IOException ioe) {
            throw new RuntimeException("Problem reading email notification template", ioe);
        }
    }
}

