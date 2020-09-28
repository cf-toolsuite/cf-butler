package io.pivotal.cfapp.domain;

import java.time.LocalDateTime;

import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Table("time_keeper")
public class TimeKeeper {

	private LocalDateTime collectionTime;
}
