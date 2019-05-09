package io.pivotal.cfapp.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import org.springframework.data.annotation.Id;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@JsonPropertyOrder({ "id", "name"})
public class Organization {

    @Id
    @JsonProperty("id")
    private final String id;

    @JsonProperty("name")
    private final String name;


    @JsonCreator
    public Organization(
        @JsonProperty("id") String id,
        @JsonProperty("name") String name) {
            this.id = id;
            this.name = name;
    }

}