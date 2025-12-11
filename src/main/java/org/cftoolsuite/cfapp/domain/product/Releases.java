
package org.cftoolsuite.cfapp.domain.product;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;

@Builder
@Getter
@JsonPropertyOrder({
    "releases"
})
public class Releases {

    @Default
    @JsonProperty("releases")
    private List<Release> releases = new ArrayList<>();

    @JsonCreator
    public Releases(@JsonProperty("releases") List<Release> releases) {
        this.releases = releases;
    }
}
