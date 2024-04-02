
package org.cftoolsuite.cfapp.domain.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Builder;
import lombok.Getter;


@Builder
@Getter
@JsonPropertyOrder({
    "self"
})
public class EulaLinks {

    @JsonProperty("self")
    private Self self;

    public EulaLinks(
            @JsonProperty("self") Self self
            ) {
        this.self = self;
    }

}
