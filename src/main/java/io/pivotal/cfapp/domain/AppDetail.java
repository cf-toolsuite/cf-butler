package io.pivotal.cfapp.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@AllArgsConstructor(access=AccessLevel.PACKAGE)
@NoArgsConstructor(access=AccessLevel.PACKAGE)
@Getter
@EqualsAndHashCode
@ToString
@Table("application_detail")
public class AppDetail {

    public static AppDetailBuilder from(AppDetail detail) {
        return AppDetail
                .builder()
                .pk(detail.getPk())
                .organization(detail.getOrganization())
                .space(detail.getSpace())
                .appId(detail.getAppId())
                .appName(detail.getAppName())
                .buildpack(detail.getBuildpack())
                .buildpackVersion(detail.getBuildpackVersion())
                .image(detail.getImage())
                .stack(detail.getStack())
                .runningInstances(detail.getRunningInstances())
                .totalInstances(detail.getTotalInstances())
                .memoryUsed(detail.getMemoryUsed())
                .diskUsed(detail.getDiskUsed())
                .urls(detail.getUrls())
                .lastPushed(detail.getLastPushed())
                .lastEvent(detail.getLastEvent())
                .lastEventActor(detail.getLastEventActor())
                .lastEventTime(detail.getLastEventTime())
                .requestedState(detail.getRequestedState())
                .buildpackReleaseType(detail.getBuildpackReleaseType())
                .buildpackReleaseDate(detail.getBuildpackReleaseDate())
                .buildpackLatestVersion(detail.getBuildpackLatestVersion())
                .buildpackLatestUrl(detail.getBuildpackLatestUrl());
    }
    public static String headers() {
        return String.join(",", "organization", "space", "application id", "application name", "buildpack", "buildpack version", "image",
                "stack", "running instances", "total instances", "memory used (in gb)", "disk used (in gb)", "urls", "last pushed", "last event",
                "last event actor", "last event time", "requested state",
                "latest buildpack release type", "latest buildpack release date", "latest buildpack version", "latest buildpack Url" );
    }
    private static String wrap(String value) {
        return value != null ? StringUtils.wrap(value, '"') : StringUtils.wrap("", '"');
    }
    @Id
    @JsonIgnore
    private Long pk;
    private String organization;
    private String space;
    private String appId;
    private String appName;
    private String buildpack;
    private String buildpackVersion;
    private String image;
    private String stack;
    private Integer runningInstances;
    private Integer totalInstances;
    private Long memoryUsed;
    private Long diskUsed;
    @Default
    private List<String> urls = new ArrayList<>();
    private LocalDateTime lastPushed;
    private String lastEvent;
    private String lastEventActor;
    private LocalDateTime lastEventTime;
    private String requestedState;
    private String buildpackReleaseType;

    private LocalDateTime buildpackReleaseDate;

    private String buildpackLatestVersion;

    private String buildpackLatestUrl;

    public String toCsv() {
        return String.join(",", wrap(getOrganization()), wrap(getSpace()), wrap(getAppId()), wrap(getAppName()),
                wrap(getBuildpack()), wrap(getBuildpackVersion()), wrap(getImage()), wrap(getStack()), wrap(String.valueOf(getRunningInstances())),
                wrap(String.valueOf(getTotalInstances())), wrap(Double.toString(toGigabytes(getMemoryUsed()))),
                wrap(Double.toString(toGigabytes(getDiskUsed()))),
                (wrap(String.join(",", getUrls() != null ? getUrls(): Collections.emptyList()))),
                wrap(getLastPushed() != null ? getLastPushed().toString() : ""), wrap(getLastEvent()),
                wrap(getLastEventActor()), wrap(getLastEventTime() != null ? getLastEventTime().toString() : ""),
                wrap(getRequestedState()),
                wrap(getBuildpackReleaseType()),
                wrap(getBuildpackReleaseDate() != null ? getBuildpackReleaseDate().toString() : ""),
                wrap(getBuildpackLatestVersion()),
                wrap(getBuildpackLatestUrl()));
    }

    private Double toGigabytes(Long input) {
        return Double.valueOf(input / 1000000000.0);
    }
}
