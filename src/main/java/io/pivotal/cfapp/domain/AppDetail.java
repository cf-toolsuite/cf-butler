package io.pivotal.cfapp.domain;

import java.time.LocalDateTime;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Id;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@AllArgsConstructor(access=AccessLevel.PACKAGE)
@NoArgsConstructor(access=AccessLevel.PACKAGE)
@Getter
@ToString
public class AppDetail {

	private String organization;
	private String space;
	@Id
	private String appId;
	private String appName;
	private String buildpack;
	private String image;
	private String stack;
	private Integer runningInstances;
	private Integer totalInstances;
	private String urls;
	private LocalDateTime lastPushed;
	private String lastEvent;
	private String lastEventActor;
	private LocalDateTime lastEventTime;
	private String requestedState;

	public String toCsv() {
		return String.join(",", wrap(getOrganization()), wrap(getSpace()), wrap(getAppId()), wrap(getAppName()),
				wrap(getBuildpack()), wrap(getImage()), wrap(getStack()), wrap(String.valueOf(getRunningInstances())),
				wrap(String.valueOf(getTotalInstances())), wrap(getUrls()),
				wrap(getLastPushed() != null ? getLastPushed().toString() : ""), wrap(getLastEvent()),
				wrap(getLastEventActor()), wrap(getLastEventTime() != null ? getLastEventTime().toString() : ""),
				wrap(getRequestedState()));
	}

	private String wrap(String value) {
		return value != null ? StringUtils.wrap(value, '"') : StringUtils.wrap("", '"');
	}

	public static String headers() {
		return String.join(",", "organization", "space", "application id", "application name", "buildpack", "image",
				"stack", "running instances", "total instances", "urls", "last pushed", "last event",
				"last event actor", "last event time", "requested state");
	}
	
	public static AppDetailBuilder from(AppDetail detail) {
        return AppDetail
        		.builder()
                    .organization(detail.getOrganization())
                    .space(detail.getSpace())
                    .appId(detail.getAppId())
                    .appName(detail.getAppName())
                    .buildpack(detail.getBuildpack())
                    .image(detail.getImage())
                    .stack(detail.getStack())
                    .runningInstances(detail.getRunningInstances())
                    .totalInstances(detail.getTotalInstances())
                    .urls(detail.getUrls())
                    .lastPushed(detail.getLastPushed())
                    .lastEvent(detail.getLastEvent())
                    .lastEventActor(detail.getLastEventActor())
                    .lastEventTime(detail.getLastEventTime())
                    .requestedState(detail.getRequestedState());
    }
}
