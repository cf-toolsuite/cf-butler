package io.pivotal.cfapp.domain;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import reactor.util.function.Tuple3;

@Builder
@AllArgsConstructor(access=AccessLevel.PACKAGE)
@NoArgsConstructor(access=AccessLevel.PACKAGE)
@Getter
@ToString
@JsonPropertyOrder({ "by-organization", "by-service", "by-service-and-plan", "total-service-instances", "velocity" })
public class ServiceInstanceCounts {

    @JsonProperty("by-organization")
    private Map<String,Long> byOrganization;

    @JsonProperty("by-service")
    private Map<String,Long> byService;

    @JsonProperty("by-service-and-plan")
    private List<Tuple3<String, String, Long>> byServiceAndPlan;

    @JsonProperty("total-service-instances")
    private Long totalServiceInstances;

    @JsonProperty("velocity")
    private Map<String,Long> velocity;
}
