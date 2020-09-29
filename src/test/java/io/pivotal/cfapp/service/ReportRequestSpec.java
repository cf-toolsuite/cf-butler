package io.pivotal.cfapp.service;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Builder;
import lombok.Getter;


@Builder
@Getter
@JsonPropertyOrder({ "input", "output" })
public class ReportRequestSpec {

    @JsonProperty("input")
    private final ReportRequest[] input;
    @JsonProperty("output")
    private final String output;

    @JsonCreator
    public ReportRequestSpec(
            @JsonProperty("input") ReportRequest[] input,
            @JsonProperty("output") String output) {
        this.input = input;
        this.output = output;
    }
}
