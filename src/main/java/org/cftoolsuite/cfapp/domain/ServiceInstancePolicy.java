package org.cftoolsuite.cfapp.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
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
@JsonPropertyOrder({ "id", "git-commit", "operation", "description", "options", "organization-whitelist", "cron-expression" })
@Getter
@ToString
public class ServiceInstancePolicy implements HasOrganizationWhiteList, Policy {

    public static ServiceInstancePolicy seed(ServiceInstancePolicy policy) {
        return ServiceInstancePolicy
                .builder()
                .description(policy.getDescription())
                .operation(policy.getOperation())
                .options(policy.getOptions())
                .organizationWhiteList(policy.getOrganizationWhiteList())
                .cronExpression(policy.getCronExpression())
                .build();
    }

    public static ServiceInstancePolicy seedWith(ServiceInstancePolicy policy, String gitCommit) {
        return ServiceInstancePolicy
                .builder()
                .gitCommit(gitCommit)
                .description(policy.getDescription())
                .operation(policy.getOperation())
                .options(policy.getOptions())
                .organizationWhiteList(policy.getOrganizationWhiteList())
                .cronExpression(policy.getCronExpression())
                .build();
    }

    public static String tableName() {
        return "service_instance_policy";
    }

    @Id
    @JsonIgnore
    private Long pk;

    @Default
    @JsonProperty("id")
    private String id = Generators.timeBasedGenerator().generate().toString();

    @JsonProperty("git-commit")
    @Column("git_commit")
    private String gitCommit;

    @JsonProperty("operation")
    private String operation;

    @JsonProperty("description")
    private String description;

    @Default
    @JsonProperty("options")
    private Map<String, Object> options = new HashMap<>();

    @Default
    @JsonProperty("organization-whitelist")
    private Set<String> organizationWhiteList = new HashSet<>();

    @JsonProperty("cron-expression")
    @Column("cron_expression")
    private String cronExpression;

    @JsonCreator
    ServiceInstancePolicy(
            @JsonProperty("pk") Long pk,
            @JsonProperty("id") String id,
            @JsonProperty("git-commit") String gitCommit,
            @JsonProperty("operation") String operation,
            @JsonProperty("description") String description,
            @JsonProperty("options") Map<String, Object> options,
            @JsonProperty("organization-whitelist") Set<String> organizationWhiteList,
            @JsonProperty("cron-expression") String cronExpression) {
        this.pk = pk;
        this.id = id;
        this.gitCommit = gitCommit;
        this.operation = operation;
        this.description = description;
        this.options = options;
        this.organizationWhiteList = organizationWhiteList;
        this.cronExpression = cronExpression;
    }

    public String getCronExpression() {
        return StringUtils.isBlank(cronExpression) ? defaultCronExpression(): cronExpression;
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
