package io.pivotal.cfapp.service;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@JsonPropertyOrder({ "foundation", "environment", "period", "filename" })
class ReportRequest {

    @JsonProperty("foundation")
    private final String foundation;
    @JsonProperty("environment")
    private final String environment;
    @JsonProperty("period")
    private final String period;
    @JsonProperty("filename")
    private final String filename;

    @JsonCreator
    public ReportRequest(
        @JsonProperty("foundation") String foundation,
        @JsonProperty("environment") String environment,
        @JsonProperty("period") String period,
        @JsonProperty("filename") String filename) {
        this.foundation = foundation;
        this.environment = environment;
        this.period = period;
        this.filename = filename;
    }

}