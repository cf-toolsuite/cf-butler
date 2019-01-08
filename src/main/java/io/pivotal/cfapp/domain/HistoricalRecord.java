package io.pivotal.cfapp.domain;

import java.time.LocalDateTime;

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
	private String status;
	private String errorDetails;
	
}
