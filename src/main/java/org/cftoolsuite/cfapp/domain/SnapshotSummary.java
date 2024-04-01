package org.cftoolsuite.cfapp.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
@JsonPropertyOrder({ "application-counts", "service-instance-counts", "user-counts" })
public class SnapshotSummary {

    @JsonProperty("application-counts")
    private ApplicationCounts applicationCounts;

    @JsonProperty("service-instance-counts")
    private ServiceInstanceCounts serviceInstanceCounts;

    @JsonProperty("user-counts")
    private UserCounts userCounts;

}
