package io.pivotal.cfapp.domain.product;

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
    "_links"
    })
public class Eula {

    @Default
    @JsonProperty("id")
    private Long id = -1L;

    @JsonProperty("slug")
    private String slug;

    @JsonProperty("name")
    private String name;

    @JsonProperty("_links")
    private EulaLinks links;

    @JsonCreator
    public Eula(
        @JsonProperty("id") Long id,
        @JsonProperty("slug") String slug,
        @JsonProperty("name") String name,
        @JsonProperty("_links") EulaLinks links
    ) {
        this.id = id;
        this.slug = slug;
        this.name = name;
        this.links = links;
    }
}