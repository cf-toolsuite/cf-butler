package io.pivotal.cfapp.domain;

import java.time.LocalDateTime;

import org.springframework.util.CollectionUtils;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AppDetailShim {

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
	private String urls;
	private LocalDateTime lastPushed;
	private String lastEvent;
	private String lastEventActor;
	private LocalDateTime lastEventTime;
	private String requestedState;
	private String buildpackReleaseType;
	private LocalDateTime buildpackReleaseDate;
	private String buildpackLatestVersion;
	private String buildpackLatestUrl;

	public static AppDetailShim from(AppDetail detail) {
        return AppDetailShim
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
						.urls(CollectionUtils.isEmpty(detail.getUrls()) ? null : String.join(",", detail.getUrls()))
						.lastPushed(detail.getLastPushed())
						.lastEvent(detail.getLastEvent())
						.lastEventActor(detail.getLastEventActor())
						.lastEventTime(detail.getLastEventTime())
						.requestedState(detail.getRequestedState())
						.buildpackReleaseType(detail.getBuildpackReleaseType())
						.buildpackReleaseDate(detail.getBuildpackReleaseDate())
						.buildpackLatestVersion(detail.getBuildpackLatestVersion())
						.buildpackLatestUrl(detail.getBuildpackLatestUrl())
						.build();
	}

}
