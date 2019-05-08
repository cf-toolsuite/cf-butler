package io.pivotal.cfapp.domain;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.Builder.Default;

@Builder
@AllArgsConstructor(access=AccessLevel.PACKAGE)
@NoArgsConstructor(access=AccessLevel.PACKAGE)
@Getter
@ToString
@JsonPropertyOrder({ "by-organization", "by-buildpack", "by-stack", "by-dockerimage", "by-status",
"total-applications", "total-running-application-instances", "total-stopped-application-instances", "total-crashed-application-instances",
"total-application-instances", "total-memory-used-in-mb", "total-disk-used-in-mb", "velocity"})
public class ApplicationCounts {

    @Default
    @JsonProperty("by-organization")
    private Map<String, Long> byOrganization = new HashMap<>();

    @Default
    @JsonProperty("by-buildpack")
    private Map<String, Long> byBuildpack = new HashMap<>();

    @Default
    @JsonProperty("by-stack")
    private Map<String, Long> byStack = new HashMap<>();

    @Default
    @JsonProperty("by-dockerimage")
    private Map<String, Long> byDockerImage = new HashMap<>();

    @Default
    @JsonProperty("by-status")
    private Map<String, Long> byStatus = new HashMap<>();

    @Default
    @JsonProperty("total-applications")
    private Long totalApplications = 0L;

    @Default
    @JsonProperty("total-running-application-instances")
    private Long totalRunningApplicationInstances = 0L;

    @Default
    @JsonProperty("total-stopped-application-instances")
    private Long totalStoppedApplicationInstances = 0L;

    @Default
    @JsonProperty("total-crashed-application-instances")
    private Long totalCrashedApplicationInstances = 0L;

    @Default
    @JsonProperty("total-application-instances")
    private Long totalApplicationInstances = 0L;

    @Default
    @JsonProperty("total-memory-used-in-gb")
    private Double totalMemoryUsed = 0.0;

    @Default
    @JsonProperty("total-disk-used-in-gb")
    private Double totalDiskUsed = 0.0;

    @Default
    @JsonProperty("velocity")
    private Map<String, Long> velocity = new HashMap<>();
}
