package io.pivotal.cfapp.domain;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.mapping.OutboundRow;
import org.springframework.r2dbc.core.Parameter;
import org.springframework.stereotype.Indexed;

@Indexed
@WritingConverter
public class ServiceInstanceDetailWriteConverter implements Converter<ServiceInstanceDetail, OutboundRow> {

    @Override
    public OutboundRow convert(ServiceInstanceDetail source) {
        OutboundRow row = new OutboundRow();
        row.put("organization", Parameter.fromOrEmpty(source.getOrganization(), String.class));
        row.put("space", Parameter.fromOrEmpty(source.getSpace(), String.class));
        row.put("service_instance_id", Parameter.fromOrEmpty(source.getServiceInstanceId(), String.class));
        row.put("service_name", Parameter.fromOrEmpty(source.getName(), String.class));
        row.put("service", Parameter.fromOrEmpty(source.getService(), String.class));
        row.put("description", Parameter.fromOrEmpty(source.getDescription(), String.class));
        row.put("plan", Parameter.fromOrEmpty(source.getPlan(), String.class));
        row.put("type", Parameter.fromOrEmpty(source.getType(), String.class));
        row.put("bound_applications", Parameter.fromOrEmpty(source.getApplications().stream().filter(StringUtils::isNotBlank).collect(Collectors.joining(",")), String.class));
        row.put("last_operation", Parameter.fromOrEmpty(source.getLastOperation(), String.class));
        row.put("dashboard_url", Parameter.fromOrEmpty(source.getDashboardUrl(), String.class));
        row.put("requested_state", Parameter.fromOrEmpty(source.getRequestedState(), String.class));
        row.put("last_updated", Parameter.fromOrEmpty(source.getLastUpdated(), LocalDateTime.class));
        return row;
    }

}
