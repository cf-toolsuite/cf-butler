package io.pivotal.cfapp.domain;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.r2dbc.spi.Row;

@ReadingConverter
public class HygienePolicyReadConverter implements Converter<Row, HygienePolicy> {
    
	private ObjectMapper mapper = new ObjectMapper();
	
    public HygienePolicy convert(Row source) {
        return
    		HygienePolicy
				.builder()
					.pk(source.get("pk", Long.class))
					.id(source.get("id", String.class))
					.daysSinceLastUpdate(source.get("days_since_last_update", Integer.class))
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

