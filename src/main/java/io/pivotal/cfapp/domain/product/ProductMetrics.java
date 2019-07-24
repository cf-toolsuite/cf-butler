package io.pivotal.cfapp.domain.product;

import java.util.Set;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Builder;
import lombok.Getter;
import lombok.Builder.Default;

@Builder
@Getter
@JsonPropertyOrder({ "product-metrics "})
public class ProductMetrics {

    @Default
    @JsonProperty("product-metrics")
    Set<ProductMetric> productMetrics = new TreeSet<>();

    @JsonCreator
    public ProductMetrics(Set<ProductMetric> productMetrics) {
        this.productMetrics = productMetrics;
    }
}