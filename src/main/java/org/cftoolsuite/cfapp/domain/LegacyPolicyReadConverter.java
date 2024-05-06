package org.cftoolsuite.cfapp.domain;

import java.io.IOException;

import org.cftoolsuite.cfapp.util.CsvUtil;
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
                    .gitCommit(source.get("git_commit", String.class))
                    .stacks(CsvUtil.parse(source.get("stacks", String.class)))
                    .serviceOfferings(CsvUtil.parse(source.get("service_offerings", String.class)))
                    .operatorTemplate(readEmailNotificationTemplate(source.get("operator_email_template", String.class) == null ? "{}": source.get("operator_email_template", String.class)))
                    .notifyeeTemplate(readEmailNotificationTemplate(source.get("notifyee_email_template", String.class) == null ? "{}": source.get("notifyee_email_template", String.class)))
                    .organizationWhiteList(CsvUtil.parse(source.get("organization_whitelist", String.class)))
                    .cronExpression(source.get("cron_expression", String.class))
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

