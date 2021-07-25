package io.pivotal.cfapp.domain;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Indexed;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.r2dbc.spi.Row;

@Indexed
@ReadingConverter
public class LegacyPolicyReadConverter implements Converter<Row, LegacyPolicy> {

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public LegacyPolicy convert(Row source) {
        return
                LegacyPolicy
                .builder()
                .pk(source.get("pk", Long.class))
                .id(source.get("id", String.class))
                .stacks(source.get("stacks", String.class) != null ? new HashSet<String>(Arrays.asList(source.get("stacks", String.class).split("\\s*,\\s*"))): new HashSet<>())
                .serviceOfferings(source.get("service_offerings", String.class) != null ? new HashSet<String>(Arrays.asList(source.get("service_offerings", String.class).split("\\s*,\\s*"))): new HashSet<>())
                .operatorTemplate(readEmailNotificationTemplate(source.get("operator_email_template", String.class) == null ? "{}": source.get("operator_email_template", String.class)))
                .notifyeeTemplate(readEmailNotificationTemplate(source.get("notifyee_email_template", String.class) == null ? "{}": source.get("notifyee_email_template", String.class)))
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

