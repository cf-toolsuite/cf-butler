package org.cftoolsuite.cfapp.domain;


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
@JsonPropertyOrder({ "resources"})
public class Resources {

    @Default
    @JsonProperty("resources")
    private List<Resource> resources = new ArrayList<>();

    @JsonProperty("pagination")
    private Pagination pagination;

    @JsonCreator
    public Resources(   @JsonProperty("resources") List<Resource> resources,
                        @JsonProperty("pagination") Pagination pagination
    ) {
        this.resources = resources;
        this.pagination = pagination;
    }
}