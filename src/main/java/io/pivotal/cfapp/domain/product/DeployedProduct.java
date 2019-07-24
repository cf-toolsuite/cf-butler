package io.pivotal.cfapp.domain.product;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@JsonPropertyOrder({
    "installation_name",
    "guid",
    "type",
    "product_version",
    "label",
    "service_broker",
    "stale"
})
public class DeployedProduct {

    @JsonProperty("installation_name")
    private String installationName;

    @JsonProperty("guid")
    private String guid;

    @JsonProperty("type")
    private String type;

    @JsonProperty("product_version")
    private String productVersion;

    @JsonProperty("label")
    private String label;

    @JsonProperty("service_broker")
    private Boolean serviceBroker;

    @JsonProperty("stale")
    private Staleness stale;

    @JsonCreator
    public DeployedProduct(
        @JsonProperty("installation_name") String installationName,
        @JsonProperty("guid") String guid,
        @JsonProperty("type") String type,
        @JsonProperty("product_version") String productVersion,
        @JsonProperty("label") String label,
        @JsonProperty("service_broker") Boolean serviceBroker,
        @JsonProperty("stale") Staleness stale
    ) {
        this.installationName = installationName;
        this.guid = guid;
        this.type = type;
        this.productVersion = productVersion;
        this.label = label;
        this.serviceBroker = serviceBroker;
        this.stale = stale;
    }
}