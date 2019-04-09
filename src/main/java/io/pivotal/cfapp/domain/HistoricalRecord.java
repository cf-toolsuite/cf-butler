package io.pivotal.cfapp.domain;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Id;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor(access=AccessLevel.PACKAGE)
@NoArgsConstructor(access=AccessLevel.PACKAGE)
@Getter
public class HistoricalRecord {

	@Id
	@JsonIgnore
	private Long pk;
	private LocalDateTime transactionDateTime;
	private String actionTaken;
	private String organization;
	private String space;
	private String appId;
	private String serviceInstanceId;
	private String type;
	private String name;

	public String toCsv() {
		return String.join(",",
				wrap(getTransactionDateTime() != null ? getTransactionDateTime().toString() : ""),
				wrap(getActionTaken()), wrap(getOrganization()), wrap(getSpace()), wrap(getAppId()),
				wrap(getServiceInstanceId()), wrap(getType()), wrap(getName()));
	}

	private String wrap(String value) {
		return value != null ? StringUtils.wrap(value, '"') : StringUtils.wrap("", '"');
	}

	public static String headers() {
        return String.join(",", "transaction date/time", "action taken", "organization", "space",
                "application id", "service instance id", "type", "name");
    }

}
