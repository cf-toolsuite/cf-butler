package org.cftoolsuite.cfapp.domain;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "endpoint", "json-path-expression", "apply-json-to-csv-converter" })
@Getter
@ToString
public class EndpointRequest {

    @JsonProperty("endpoint")
    private String endpoint;

    @JsonProperty("json-path-expression")
    private String jsonPathExpression;

    @JsonProperty("apply-json-to-csv-converter")
    private boolean applyJsonToCsvConverter;

    @JsonCreator
    public EndpointRequest(
        @JsonProperty("endpoint") String endpoint,
        @JsonProperty("json-path-expression") String jsonPathExpression,
        @JsonProperty("apply-json-to-csv-converter") boolean applyJsonToCsvConverter
    ) {
        this.endpoint = endpoint;
        this.jsonPathExpression = jsonPathExpression;
        this.applyJsonToCsvConverter = applyJsonToCsvConverter;
    }

}