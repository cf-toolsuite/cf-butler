package org.cftoolsuite.cfapp.domain.product;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@JsonPropertyOrder({ "os", "version" })
public class StemcellDetail {

    @JsonProperty("os")
    private String os;

    @JsonProperty("version")
    private String version;

    @JsonCreator
    public StemcellDetail(
            @JsonProperty("os") String os,
            @JsonProperty("version") String version
            ) {
        this.os = os;
        this.version = version;
    }
}
