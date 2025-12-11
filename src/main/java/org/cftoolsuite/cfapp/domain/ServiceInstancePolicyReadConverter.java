package org.cftoolsuite.cfapp.domain;

import java.util.Map;

import org.cftoolsuite.cfapp.util.CsvUtil;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Indexed;

import io.r2dbc.spi.Row;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Indexed
@ReadingConverter
public class ServiceInstancePolicyReadConverter implements Converter<Row, ServiceInstancePolicy> {

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public ServiceInstancePolicy convert(Row source) {
        return
            ServiceInstancePolicy
                .builder()
                    .pk(source.get("pk", Long.class))
                    .id(source.get("id", String.class))
                    .gitCommit(source.get("git_commit", String.class))
                    .operation(source.get("operation", String.class))
                    .description(source.get("description", String.class))
                    .options(readOptions(source.get("options", String.class) == null ? "{}" : source.get("options", String.class)))
                    .organizationWhiteList(CsvUtil.parse(source.get("organization_whitelist", String.class)))
                    .cronExpression(source.get("cron_expression", String.class))
                    .build();
    }

    private Map<String, Object> readOptions(String value) {
        try {
            return mapper.readValue(value, new TypeReference<Map<String, Object>>() {});
        } catch (JacksonException je) {
            throw new RuntimeException("Problem reading options", je);
        }
    }
}
