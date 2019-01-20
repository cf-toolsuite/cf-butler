package io.pivotal.cfapp.domain;

import java.time.LocalDateTime;

import org.apache.commons.lang3.StringUtils;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@AllArgsConstructor(access=AccessLevel.PACKAGE)
@NoArgsConstructor(access=AccessLevel.PACKAGE)
@ToString
public class HistoricalRecord {


	private LocalDateTime transactionDateTime;
	private String actionTaken;
	private String organization;
	private String space;
	private String appId;
	private String serviceId;
	private String type;
	private String name;
	
	public static String headers() {
        return String.join(",", "transaction date/time", "action taken", "organization", "space",
                "application id", "service id", "type", "name");
    }
	
	public String toCsv() {
        return String
                .join(",", wrap(getTransactionDateTime() != null ? getTransactionDateTime().toString(): ""),
                		wrap(getActionTaken()), wrap(getOrganization()), wrap(getSpace()), wrap(getAppId()),
                        wrap(getServiceId()), wrap(getType()), wrap(getName()));
    }
    
    private String wrap(String value) {
        return value != null ? StringUtils.wrap(value, '"') : StringUtils.wrap("", '"');
    }
}
