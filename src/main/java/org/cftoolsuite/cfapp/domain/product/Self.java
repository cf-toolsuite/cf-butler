
package org.cftoolsuite.cfapp.domain.product;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@JsonPropertyOrder({
    "href"
})
public class Self {

    @JsonProperty("href")
    private String href;

    @JsonCreator
    public Self(@JsonProperty("href") String href) {
        this.href = href;
    }

}
