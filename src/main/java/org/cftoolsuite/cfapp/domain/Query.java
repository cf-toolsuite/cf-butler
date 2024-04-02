package org.cftoolsuite.cfapp.domain;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Builder;
import lombok.Getter;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "description", "sql" })
@Getter
public class Query {

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("sql")
    private String sql;

    @JsonCreator
    public Query(
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("sql") String sql
            ) {
        this.name = name;
        this.description = description;
        this.sql = sql;
    }

    @JsonIgnore
    public boolean isValid() {
        return StringUtils.isNotBlank(name)
                && StringUtils.isNotBlank(sql)
                && sql.toLowerCase().startsWith("select");
    }
}
