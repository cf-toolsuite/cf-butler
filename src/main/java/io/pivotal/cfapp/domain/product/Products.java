
package io.pivotal.cfapp.domain.product;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Builder;
import lombok.Getter;
import lombok.Builder.Default;


@Builder
@Getter
@JsonPropertyOrder({
    "products"
})
public class Products {

    @Default
    @JsonProperty("products")
    private List<Product> products = new ArrayList<>();

    @JsonCreator
    public Products(@JsonProperty("products") List<Product> products) {
        this.products = products;
    }
}
