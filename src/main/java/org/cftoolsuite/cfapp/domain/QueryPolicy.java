package org.cftoolsuite.cfapp.domain;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
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
@JsonPropertyOrder({ "id", "git-commit", "description", "queries", "email-notification-template", "cron-expression" })
@Getter
@ToString
@Table("query_policy")
public class QueryPolicy implements Policy {

    public static QueryPolicy seed(QueryPolicy policy) {
        return QueryPolicy
                .builder()
                .description(policy.getDescription())
                .queries(policy.getQueries())
                .emailNotificationTemplate(policy.getEmailNotificationTemplate())
                .cronExpression(policy.getCronExpression())
                .build();
    }

    public static QueryPolicy seedWith(QueryPolicy policy, String gitCommit) {
        return QueryPolicy
                .builder()
                .gitCommit(gitCommit)
                .description(policy.getDescription())
                .queries(policy.getQueries())
                .emailNotificationTemplate(policy.getEmailNotificationTemplate())
                .cronExpression(policy.getCronExpression())
                .build();
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

    @JsonProperty("description")
    private String description;

    @Default
    @JsonProperty("queries")
    private Set<Query> queries = new HashSet<>();

    @JsonProperty("email-notification-template")
    private EmailNotificationTemplate emailNotificationTemplate;

    @JsonProperty("cron-expression")
    @Column("cron_expression")
    private String cronExpression;

    @JsonCreator
    QueryPolicy(
            @JsonProperty("pk") Long pk,
            @JsonProperty("id") String id,
            @JsonProperty("git-commit") String gitCommit,
            @JsonProperty("description") String description,
            @JsonProperty("queries") Set<Query> queries,
            @JsonProperty("email-notification-template") EmailNotificationTemplate emailNotificationTemplate,
            @JsonProperty("cron-expression") String cronExpression) {
        this.pk = pk;
        this.id = id;
        this.gitCommit = gitCommit;
        this.description = description;
        this.queries = queries;
        this.emailNotificationTemplate = emailNotificationTemplate;
        this.cronExpression = cronExpression;
    }

    public String getCronExpression() {
        return StringUtils.isBlank(cronExpression) ? defaultCronExpression(): cronExpression;
    }

    @JsonIgnore
    public Long getPk() {
        return pk;
    }

    public Set<Query> getQueries() {
        return CollectionUtils.isEmpty(queries) ? new HashSet<>(): Collections.unmodifiableSet(queries);
    }

}
