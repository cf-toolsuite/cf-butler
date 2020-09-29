package io.pivotal.cfapp.domain;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@AllArgsConstructor(access=AccessLevel.PACKAGE)
@NoArgsConstructor(access=AccessLevel.PACKAGE)
@Getter
@ToString
@JsonPropertyOrder({ "by-organization", "by-service", "by-service-and-plan", "total-service-instances", "velocity" })
public class ServiceInstanceCounts {

    @Default
    @JsonProperty("by-organization")
    private Map<String, Long> byOrganization = new HashMap<>();

    @Default
    @JsonProperty("by-service")
    private Map<String, Long> byService = new HashMap<>();

    @Default
    @JsonProperty("by-service-and-plan")
    private Map<String, Long> byServiceAndPlan = new HashMap<>();

    @Default
    @JsonProperty("total-service-instances")
    private Long totalServiceInstances = 0L;

    @Default
    @JsonProperty("velocity")
    private Map<String, Long> velocity = new HashMap<>();
}
