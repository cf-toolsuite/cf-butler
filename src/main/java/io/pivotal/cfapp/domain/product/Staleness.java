package io.pivotal.cfapp.domain.product;

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
    "parent_products_deployed_more_recently"
})
public class Staleness {

    @Default
    @JsonProperty("parent_products_deployed_more_recently")
    private List<String> parentProductsDeployedMoreRecently = new ArrayList<>();

    @JsonCreator
    public Staleness(
            @JsonProperty("parent_products_deployed_more_recently") List<String> parentProductsDeployedMoreRecently
            ) {
        this.parentProductsDeployedMoreRecently = parentProductsDeployedMoreRecently;
    }
}
