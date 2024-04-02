package org.cftoolsuite.cfapp.domain.product;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ProductType {
    BUILDPACK("buildpack"),
    STEMCELL("stemcell"),
    TILE("tile");

    public static ProductType from(String value) {
        ProductType result = ProductType.TILE;
        if (value.contains("stemcell")) {
            result = ProductType.STEMCELL;
        } else if (value.contains("buildpack")) {
            result = ProductType.BUILDPACK;
        }
        return result;
    }

    private String id;

    ProductType(String id) {
        this.id = id;
    }

    @JsonValue
    public String getId() {
        return id;
    }
}
