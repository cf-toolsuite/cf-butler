package io.pivotal.cfapp.domain;

import java.time.LocalDateTime;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Indexed;

import io.r2dbc.spi.Row;

@Indexed
@ReadingConverter
public class ServiceInstanceDetailReadConverter implements Converter<Row, ServiceInstanceDetail> {

    @Override
    public ServiceInstanceDetail convert(Row source) {
        return
                ServiceInstanceDetail
                .builder()
                .pk(source.get("pk", Long.class))
                .organization(Defaults.getColumnValue(source, "organization", String.class))
                .space(Defaults.getColumnValue(source, "space", String.class))
                .serviceInstanceId(Defaults.getColumnValue(source, "service_instance_id", String.class))
                .name(Defaults.getColumnValue(source, "service_name", String.class))
                .service(Defaults.getColumnValue(source, "service", String.class))
                .description(Defaults.getColumnValue(source, "description", String.class))
                .type(Defaults.getColumnValue(source, "type", String.class))
                .plan(Defaults.getColumnValue(source, "plan", String.class))
                .applications(
                        Defaults.getColumnListOfStringValue(source, "bound_applications"))
                .lastOperation(Defaults.getColumnValue(source, "last_operation", String.class))
                .dashboardUrl(Defaults.getColumnValue(source, "dashboard_url", String.class))
                .lastUpdated(Defaults.getColumnValue(source, "last_updated", LocalDateTime.class))
                .requestedState(Defaults.getColumnValue(source, "requested_state", String.class))
                .build();
    }
}

