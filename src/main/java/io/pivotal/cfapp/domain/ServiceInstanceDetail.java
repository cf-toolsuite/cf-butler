package io.pivotal.cfapp.domain;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Id;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor(access=AccessLevel.PACKAGE)
@NoArgsConstructor(access=AccessLevel.PACKAGE)
@Getter
@EqualsAndHashCode
public class ServiceInstanceDetail {

	@Id
	@JsonIgnore
	private Long pk;
	private String organization;
	private String space;
	private String serviceInstanceId;
	private String name;
	private String service;
	private String description;
	private String plan;
	private String type;
	private List<String> applications;
	private String lastOperation;
	private LocalDateTime lastUpdated;
	private String dashboardUrl;
	private String requestedState;

	public String toCsv() {
		return String.join(",", wrap(getOrganization()), wrap(getSpace()), wrap(getServiceInstanceId()), wrap(getName()),
				wrap(getService()), wrap(getDescription()), wrap(getPlan()), wrap(getType()),
				wrap(String.join(",", getApplications())), wrap(getLastOperation()),
				wrap(getLastUpdated() != null ? getLastUpdated().toString() : ""), wrap(getDashboardUrl()),
				wrap(getRequestedState()));
	}

	private String wrap(String value) {
		return value != null ? StringUtils.wrap(value, '"') : StringUtils.wrap("", '"');
	}

	public static String headers() {
        return String.join(",", "organization", "space", "service instance id",
                "name", "service", "description", "plan", "type", "bound applications", "last operation", "last updated", "dashboard url", "requested state");
    }

	public static ServiceInstanceDetailBuilder from(ServiceInstanceDetail detail) {
        return ServiceInstanceDetail
				.builder()
					.pk(detail.getPk())
                    .organization(detail.getOrganization())
                    .space(detail.getSpace())
                    .serviceInstanceId(detail.getServiceInstanceId())
                    .name(detail.getName())
                    .service(detail.getService())
                    .description(detail.getDescription())
                    .plan(detail.getPlan())
                    .type(detail.getType())
                    .applications(detail.getApplications())
                    .lastOperation(detail.getLastOperation())
                    .lastUpdated(detail.getLastUpdated())
                    .dashboardUrl(detail.getDashboardUrl())
                    .requestedState(detail.getRequestedState());
    }

}
