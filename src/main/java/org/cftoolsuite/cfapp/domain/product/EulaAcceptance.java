package org.cftoolsuite.cfapp.domain.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@JsonPropertyOrder({
    "href"
})
public class EulaAcceptance {

    @JsonProperty("href")
    private String href;

    public EulaAcceptance(@JsonProperty("href") String href) {
        this.href = href;
    }
}
