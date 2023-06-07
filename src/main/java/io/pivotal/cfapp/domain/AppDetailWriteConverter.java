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
public class AppDetailWriteConverter implements Converter<AppDetail, OutboundRow> {

    @Override
    public OutboundRow convert(AppDetail source) {
        OutboundRow row = new OutboundRow();
        row.put("organization", Parameter.fromOrEmpty(source.getOrganization(), String.class));
        row.put("space", Parameter.fromOrEmpty(source.getSpace(), String.class));
        row.put("app_id", Parameter.fromOrEmpty(source.getAppId(), String.class));
        row.put("app_name", Parameter.fromOrEmpty(source.getAppName(), String.class));
        row.put("buildpack", Parameter.fromOrEmpty(source.getBuildpack(), String.class));
        row.put("buildpack_version", Parameter.fromOrEmpty(source.getBuildpackVersion(), String.class));
        row.put("running_instances", Parameter.fromOrEmpty(source.getRunningInstances(), Integer.class));
        row.put("total_instances", Parameter.fromOrEmpty(source.getTotalInstances(), Integer.class));
        row.put("memory_used", Parameter.fromOrEmpty(source.getMemoryUsed(), Long.class));
        row.put("disk_used", Parameter.fromOrEmpty(source.getDiskUsed(), Long.class));
        row.put("memory_quota", Parameter.fromOrEmpty(source.getMemoryQuota(), Long.class));
        row.put("disk_quota", Parameter.fromOrEmpty(source.getDiskQuota(), Long.class));
        row.put("image", Parameter.fromOrEmpty(source.getImage(), String.class));
        row.put("stack", Parameter.fromOrEmpty(source.getStack(), String.class));
        row.put("urls", Parameter.fromOrEmpty(source.getUrls().stream().filter(StringUtils::isNotBlank).collect(Collectors.joining(",")), String.class));
        row.put("last_pushed", Parameter.fromOrEmpty(source.getLastPushed(), LocalDateTime.class));
        row.put("last_event_time", Parameter.fromOrEmpty(source.getLastEventTime(), LocalDateTime.class));
        row.put("last_event", Parameter.fromOrEmpty(source.getLastEvent(), String.class));
        row.put("last_event_actor", Parameter.fromOrEmpty(source.getLastEventActor(), String.class));
        row.put("requested_state", Parameter.fromOrEmpty(source.getRequestedState(), String.class));
        row.put("buildpack_release_type", Parameter.fromOrEmpty(source.getBuildpackReleaseType(), String.class));
        row.put("buildpack_release_date", Parameter.fromOrEmpty(source.getBuildpackReleaseDate(), String.class));
        row.put("buildpack_latest_version", Parameter.fromOrEmpty(source.getBuildpackLatestVersion(), String.class));
        row.put("buildpack_latest_url", Parameter.fromOrEmpty(source.getBuildpackLatestUrl(), String.class));
        return row;
    }

}
