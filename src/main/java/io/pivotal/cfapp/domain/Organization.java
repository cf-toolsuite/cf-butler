package io.pivotal.cfapp.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@JsonPropertyOrder({ "id", "name"})
@ToString
@Table("organizations")
public class Organization {

    @Id
    @JsonProperty("id")
    private final String id;

    @Column("org_name")
    @JsonProperty("name")
    private final String name;


    @JsonCreator
    @PersistenceConstructor
    public Organization(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name) {
        this.id = id;
        this.name = name;
    }

}
