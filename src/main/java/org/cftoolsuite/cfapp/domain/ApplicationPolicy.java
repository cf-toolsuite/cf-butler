package org.cftoolsuite.cfapp.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.uuid.Generators;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.ToString;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id", "operation", "description", "state", "options", "organization-whitelist" })
@Getter
@ToString
@Table("application_policy")
public class ApplicationPolicy implements HasOrganizationWhiteList {

    public static ApplicationPolicy seed(ApplicationPolicy policy) {
        return ApplicationPolicy
                .builder()
                .description(policy.getDescription())
                .operation(policy.getOperation())
                .options(policy.getOptions())
                .organizationWhiteList(policy.getOrganizationWhiteList())
                .state(policy.getState())
                .build();
    }

    public static ApplicationPolicy seedWith(ApplicationPolicy policy, String id) {
        return ApplicationPolicy
                .builder()
                .id(id)
                .description(policy.getDescription())
                .operation(policy.getOperation())
                .options(policy.getOptions())
                .organizationWhiteList(policy.getOrganizationWhiteList())
                .state(policy.getState())
                .build();
    }

    @Id
    @JsonIgnore
    private Long pk;

    @Default
    @JsonProperty("id")
    private String id = Generators.timeBasedGenerator().generate().toString();

    @JsonProperty("operation")
    private String operation;

    @JsonProperty("description")
    private String description;

    @JsonProperty("state")
    private String state;

    @Default
    @JsonProperty("options")
    private Map<String, Object> options = new HashMap<>();

    @Default
    @JsonProperty("organization-whitelist")
    @Column("organization_whitelist")
    private Set<String> organizationWhiteList = new HashSet<>();

    @JsonCreator
    ApplicationPolicy(
            @JsonProperty("pk") Long pk,
            @JsonProperty("id") String id,
            @JsonProperty("operation") String operation,
            @JsonProperty("description") String description,
            @JsonProperty("state") String state,
            @JsonProperty("options") Map<String, Object> options,
            @JsonProperty("organization-whitelist") Set<String> organizationWhiteList) {
        this.pk = pk;
        this.id = id;
        this.operation = operation;
        this.description = description;
        this.state = state;
        this.options = options;
        this.organizationWhiteList = organizationWhiteList;
    }

    @JsonIgnore
    public <T> T getOption(String key, Class<T> type) {
        Assert.isTrue(StringUtils.isNotBlank(key), "Option key must not be blank.");
        Object value = options.get(key);
        if (value == null) {
            return null;
        }
        return type.cast(value);
    }

    public Map<String, Object> getOptions() {
        return CollectionUtils.isEmpty(options) ? new HashMap<>(): Collections.unmodifiableMap(options);
    }

    public Set<String> getOrganizationWhiteList() {
        return CollectionUtils.isEmpty(organizationWhiteList) ? new HashSet<>() : Collections.unmodifiableSet(organizationWhiteList);
    }

    @JsonIgnore
    public Long getPk() {
        return pk;
    }

}
