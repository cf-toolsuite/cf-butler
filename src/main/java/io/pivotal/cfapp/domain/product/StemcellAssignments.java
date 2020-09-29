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
    "products"
})
public class StemcellAssignments {

    @Default
    @JsonProperty("products")
    private List<StemcellAssignment> products = new ArrayList<>();

    @JsonCreator
    public StemcellAssignments(@JsonProperty("products") List<StemcellAssignment> products) {
        this.products = products;
    }
}
