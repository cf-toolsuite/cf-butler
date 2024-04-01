
package org.cftoolsuite.cfapp.domain.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Builder;
import lombok.Getter;


@Builder
@Getter
@JsonPropertyOrder({
    "self",
    "releases",
    "product_files",
    "file_groups"
})
public class ProductLinks {

    @JsonProperty("self")
    private Self self;

    @JsonProperty("releases")
    private Releases releases;

    @JsonProperty("product_files")
    private ProductFiles productFiles;

    @JsonProperty("file_groups")
    private FileGroups fileGroups;

    public ProductLinks(
            @JsonProperty("self") Self self,
            @JsonProperty("releases") Releases releases,
            @JsonProperty("product_files") ProductFiles productFiles,
            @JsonProperty("file_groups") FileGroups fileGroups
            ) {
        this.self = self;
        this.releases = releases;
        this.productFiles = productFiles;
        this.fileGroups = fileGroups;
    }

}
