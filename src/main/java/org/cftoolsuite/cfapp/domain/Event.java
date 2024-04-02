package org.cftoolsuite.cfapp.domain;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Event {

    private String type;
    private String actee;
    private String actor;
    private LocalDateTime time;
}
