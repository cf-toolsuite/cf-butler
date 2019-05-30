
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
    "id",
    "slug",
    "name",
    "logo_url",
    "_links",
    "platform_compatibility",
    "installs_on_pks"
})
public class Product {

    @Default
    @JsonProperty("id")
    private Long id = -1L;

    @JsonProperty("slug")
    private String slug;

    @JsonProperty("name")
    private String name;

    @JsonProperty("logo_url")
    private String logoUrl;

    @JsonProperty("_links")
    private ProductLinks links;

    @Default
    @JsonProperty("platform_compatibility")
    private List<String> platformCompatibility = new ArrayList<>();

    @JsonProperty("installs_on_pks")
    private Boolean installsOnPks;

    @JsonCreator
    public Product(
        @JsonProperty("id") Long id,
        @JsonProperty("slug") String slug,
        @JsonProperty("name") String name,
        @JsonProperty("logo_url") String logoUrl,
        @JsonProperty("_links") ProductLinks links,
        @JsonProperty("platform_compatibility") List<String> platformCompatibility,
        @JsonProperty("installs_on_pks") Boolean installsOnPks
    ) {
        this.id = id;
        this.slug = slug;
        this.name = name;
        this.logoUrl = logoUrl;
        this.links = links;
        this.platformCompatibility = platformCompatibility;
        this.installsOnPks = installsOnPks;
    }
}
