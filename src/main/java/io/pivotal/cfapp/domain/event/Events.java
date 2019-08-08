package io.pivotal.cfapp.domain.event;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Builder;
import lombok.Getter;
import lombok.Builder.Default;

@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"next_url",
"prev_url",
"resources",
"total_pages",
"total_results"
})
public class Events {

    @JsonProperty("next_url")
    private String nextUrl;

    @JsonProperty("prev_url")
    private String prevUrl;

    @Default
    @JsonProperty("resources")
    private List<Resource> resources = new ArrayList<>();

    @Default
    @JsonProperty("total_pages")
    private Integer totalPages = 0;

    @Default
    @JsonProperty("total_results")
    private Integer totalResults = 0;

    @JsonCreator
    public Events(
        @JsonProperty("next_url") String nextUrl,
        @JsonProperty("prev_url") String prevUrl,
        @JsonProperty("resources") List<Resource> resources,
        @JsonProperty("total_pages") Integer totalPages,
        @JsonProperty("total_results") Integer totalResults
    ) {
        this.nextUrl = nextUrl;
        this.prevUrl = prevUrl;
        this.resources = resources;
        this.totalPages = totalPages;
        this.totalResults = totalResults;
    }

}