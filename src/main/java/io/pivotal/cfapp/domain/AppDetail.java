package io.pivotal.cfapp.domain;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Id;

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
public class AppDetail {

	@Id
	@JsonIgnore
	private Long pk;
	private String organization;
	private String space;
	private String appId;
	private String appName;
	private String buildpack;
	private String image;
	private String stack;
	@Default
	private Integer runningInstances = 0;
	@Default
	private Integer totalInstances = 0;
	@Default
	private Long memoryUsage = 0L;
	@Default
	private Long diskUsage = 0L;
	@Default
	private List<String> urls = new ArrayList<>();
	private LocalDateTime lastPushed;
	private String lastEvent;
	private String lastEventActor;
	private LocalDateTime lastEventTime;
	private String requestedState;

	public String toCsv() {
		return String.join(",", wrap(getOrganization()), wrap(getSpace()), wrap(getAppId()), wrap(getAppName()),
				wrap(getBuildpack()), wrap(getImage()), wrap(getStack()), wrap(String.valueOf(getRunningInstances())),
				wrap(String.valueOf(getTotalInstances())), wrap(Double.toString(toGigabytes(getMemoryUsage()))),
				wrap(Double.toString(toGigabytes(getDiskUsage()))),
				(wrap(String.join(",", getUrls() != null ? getUrls(): Collections.emptyList()))),
				wrap(getLastPushed() != null ? getLastPushed().toString() : ""), wrap(getLastEvent()),
				wrap(getLastEventActor()), wrap(getLastEventTime() != null ? getLastEventTime().toString() : ""),
				wrap(getRequestedState()));
	}

	private String wrap(String value) {
		return value != null ? StringUtils.wrap(value, '"') : StringUtils.wrap("", '"');
	}

	private Double toGigabytes(Long input) {
		return Double.valueOf(input / 1000000000.0);
	}

	public static String headers() {
		return String.join(",", "organization", "space", "application id", "application name", "buildpack", "image",
				"stack", "running instances", "total instances", "memory used (in gb)", "disk used (in gb)", "urls", "last pushed", "last event",
				"last event actor", "last event time", "requested state");
	}

	public static AppDetailBuilder from(AppDetail detail) {
        return AppDetail
					.builder()
						.pk(detail.getPk())
						.organization(detail.getOrganization())
						.space(detail.getSpace())
						.appId(detail.getAppId())
						.appName(detail.getAppName())
						.buildpack(detail.getBuildpack())
						.image(detail.getImage())
						.stack(detail.getStack())
						.runningInstances(detail.getRunningInstances())
						.totalInstances(detail.getTotalInstances())
						.memoryUsage(detail.getMemoryUsage())
						.diskUsage(detail.getDiskUsage())
						.urls(detail.getUrls())
						.lastPushed(detail.getLastPushed())
						.lastEvent(detail.getLastEvent())
						.lastEventActor(detail.getLastEventActor())
						.lastEventTime(detail.getLastEventTime())
						.requestedState(detail.getRequestedState());
	}
}
