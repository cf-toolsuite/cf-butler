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

	private LocalDateTime dateTimeRemoved;
	private String organization;
	private String space;
	private String id;
	private String type;
	private String name;
	
	public static String headers() {
        return String.join(",", "date/time removed", "organization", "space",
                "id", "type", "name");
    }
	
	public String toCsv() {
        return String
                .join(",", wrap(getDateTimeRemoved() != null ? getDateTimeRemoved().toString(): ""), 
                		wrap(getOrganization()), wrap(getSpace()), wrap(getId()),
                        wrap(getType()), wrap(getName()));
    }
    
    private String wrap(String value) {
        return value != null ? StringUtils.wrap(value, '"') : StringUtils.wrap("", '"');
    }
}
