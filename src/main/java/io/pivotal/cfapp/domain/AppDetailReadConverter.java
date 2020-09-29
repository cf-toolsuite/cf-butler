package io.pivotal.cfapp.domain;

import java.time.LocalDateTime;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import io.r2dbc.spi.Row;

@ReadingConverter
public class AppDetailReadConverter implements Converter<Row, AppDetail> {

    @Override
    public AppDetail convert(Row source) {
        return
                AppDetail
                .builder()
                .pk(source.get("pk", Long.class))
                .organization(Defaults.getColumnValue(source, "organization", String.class))
                .space(Defaults.getColumnValue(source, "space", String.class))
                .appId(Defaults.getColumnValue(source, "app_id", String.class))
                .appName(Defaults.getColumnValue(source, "app_name", String.class))
                .buildpack(Defaults.getColumnValue(source, "buildpack", String.class))
                .buildpackVersion(Defaults.getColumnValue(source, "buildpack_version", String.class))
                .runningInstances(Defaults.getColumnValueOrDefault(source, "running_instances", Integer.class, 0))
                .totalInstances(Defaults.getColumnValueOrDefault(source, "total_instances", Integer.class, 0))
                .memoryUsed(Defaults.getColumnValueOrDefault(source, "memory_used", Long.class, 0L))
                .diskUsed(Defaults.getColumnValueOrDefault(source, "disk_used", Long.class, 0L))
                .image(Defaults.getColumnValue(source, "image", String.class))
                .stack(Defaults.getColumnValue(source, "stack", String.class))
                .urls(Defaults.getColumnListOfStringValue(source, "urls"))
                .lastPushed(Defaults.getColumnValue(source, "last_pushed", LocalDateTime.class))
                .lastEventTime(Defaults.getColumnValue(source, "last_event_time", LocalDateTime.class))
                .lastEvent(Defaults.getColumnValue(source, "last_event", String.class))
                .lastEventActor(Defaults.getColumnValue(source, "last_event_actor", String.class))
                .requestedState(Defaults.getColumnValue(source, "requested_state", String.class))
                .buildpackReleaseType(Defaults.getColumnValue(source, "buildpack_release_type", String.class))
                .buildpackReleaseDate(Defaults.getColumnValue(source, "buildpack_release_date", LocalDateTime.class))
                .buildpackLatestVersion(Defaults.getColumnValue(source, "buildpack_latest_version", String.class))
                .buildpackLatestUrl(Defaults.getColumnValue(source, "buildpack_latest_url", String.class))
                .build();
    }
}

