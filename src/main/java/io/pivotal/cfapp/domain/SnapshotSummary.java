package io.pivotal.cfapp.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@AllArgsConstructor(access=AccessLevel.PACKAGE)
@NoArgsConstructor(access=AccessLevel.PACKAGE)
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