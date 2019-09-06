package io.pivotal.cfapp.domain;

import java.time.LocalDateTime;

import org.springframework.util.CollectionUtils;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ServiceInstanceDetailShim {

	private Long pk;
	private String organization;
	private String space;
	private String serviceInstanceId;
	private String serviceName;
	private String service;
	private String description;
	private String plan;
	private String type;
	private String boundApplications;
	private String lastOperation;
	private LocalDateTime lastUpdated;
	private String dashboardUrl;
	private String requestedState;

	public static ServiceInstanceDetailShim from(ServiceInstanceDetail detail) {
        return ServiceInstanceDetailShim
				.builder()
					.pk(detail.getPk())
                    .organization(detail.getOrganization())
                    .space(detail.getSpace())
                    .serviceInstanceId(detail.getServiceInstanceId())
                    .serviceName(detail.getName())
                    .service(detail.getService())
                    .description(detail.getDescription())
                    .plan(detail.getPlan())
                    .type(detail.getType())
                    .boundApplications(CollectionUtils.isEmpty(detail.getApplications()) ? null : String.join(",", detail.getApplications()))
                    .lastOperation(detail.getLastOperation())
                    .lastUpdated(detail.getLastUpdated())
                    .dashboardUrl(detail.getDashboardUrl())
					.requestedState(detail.getRequestedState())
					.build();
    }

}
