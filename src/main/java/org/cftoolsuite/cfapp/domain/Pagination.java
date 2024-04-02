package org.cftoolsuite.cfapp.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@JsonPropertyOrder({ "pagination"})
public class Pagination {    


    @JsonProperty("total_results")
    private Integer totalResult;

    @JsonProperty("total_pages")
    private Integer totalPages;


    @JsonProperty("next")
    private Href next;


    @JsonProperty("previous")
    private Href previous;


    @JsonProperty("first")
    private Href first;


    @JsonProperty("last")
    private Href last;

    @JsonCreator
    public Pagination(@JsonProperty("total_results") Integer totalResult,
    @JsonProperty("total_pages") Integer totalPages,
    @JsonProperty("previous") Href previous,
    @JsonProperty("next") Href next,
    @JsonProperty("first") Href first,
    @JsonProperty("last") Href last
    ) {
        this.totalResult = totalResult;
        this.totalPages = totalPages;
        this.previous = previous;
        this.next = next;
        this.first = first;
        this.last = last;
    }
}