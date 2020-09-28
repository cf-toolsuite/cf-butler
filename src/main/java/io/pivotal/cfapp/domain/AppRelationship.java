package io.pivotal.cfapp.domain;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
@Table("application_relationship")
public class AppRelationship {

	@Id
	@JsonIgnore
	private Long pk;
	private String organization;
	private String space;
	private String appId;
	private String appName;
	private String serviceInstanceId;
	private String serviceName;
	private String serviceOffering;
	private String servicePlan;
	private String serviceType;

	public String toCsv() {
		return String.join(",", wrap(getOrganization()), wrap(getSpace()), wrap(getAppId()), wrap(getAppName()),
				wrap(getServiceInstanceId()), wrap(getServiceName()), wrap(getServiceOffering()), wrap(getServicePlan()), wrap(getServiceType()));
	}

	private static String wrap(String value) {
		return value != null ? StringUtils.wrap(value, '"') : StringUtils.wrap("", '"');
	}

	public static String headers() {
        return String.join(",", "organization", "space", "application id",
                "application name", "service instance id", "service name", "service offering", "service plan", "service type");
    }

}
