package io.pivotal.cfapp.domain;

import java.time.LocalDateTime;

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

	private static String wrap(String value) {
		return value != null ? StringUtils.wrap(value, '"') : StringUtils.wrap("", '"');
	}

	public static String tableName() {
		return "historical_record";
	}

	public static String[] columnNames() {
		return
			new String[] {
				"pk", "transaction_date_time", "action_taken", "organization", "space", "app_id",
				"service_instance_id", "type", "name" };
	}

	public static String headers() {
        return String.join(",", "transaction date/time", "action taken", "organization", "space",
                "application id", "service instance id", "type", "name");
    }

}
