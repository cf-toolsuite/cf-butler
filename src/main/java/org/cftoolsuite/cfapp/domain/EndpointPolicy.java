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
@JsonPropertyOrder({ "id", "git-commit", "description", "endpoint-requests", "email-notification-template", "cron-expression" })
@Getter
@ToString
@Table("endpoint_policy")
public class EndpointPolicy implements Policy {

    public static EndpointPolicy seed(EndpointPolicy policy) {
        return EndpointPolicy
                .builder()
                .cronExpression(policy.getCronExpression())
                .description(policy.getDescription())
                .endpointRequests(policy.getEndpointRequests())
                .emailNotificationTemplate(policy.getEmailNotificationTemplate())
                .build();
    }

    public static EndpointPolicy seedWith(EndpointPolicy policy, String gitCommit) {
        return EndpointPolicy
                .builder()
                .gitCommit(gitCommit)
                .cronExpression(policy.getCronExpression())
                .description(policy.getDescription())
                .endpointRequests(policy.getEndpointRequests())
                .emailNotificationTemplate(policy.getEmailNotificationTemplate())
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
    @Column("endpoint_requests")
    @JsonProperty("endpoint-requests")
    private Set<EndpointRequest> endpointRequests = new HashSet<>();

    @JsonProperty("email-notification-template")
    private EmailNotificationTemplate emailNotificationTemplate;

    @JsonProperty("cron-expression")
    @Column("cron_expression")
    private String cronExpression;

    @JsonCreator
    EndpointPolicy(
            @JsonProperty("pk") Long pk,
            @JsonProperty("id") String id,
            @JsonProperty("git-commit") String gitCommit,
            @JsonProperty("description") String description,
            @JsonProperty("endpoint-requests") Set<EndpointRequest> endpointRequests,
            @JsonProperty("email-notification-template") EmailNotificationTemplate emailNotificationTemplate,
            @JsonProperty("cron-expression") String cronExpression) {
        this.pk = pk;
        this.id = id;
        this.gitCommit = gitCommit;
        this.description = description;
        this.endpointRequests = endpointRequests;
        this.emailNotificationTemplate = emailNotificationTemplate;
        this.cronExpression = cronExpression;
    }

    public String getCronExpression() {
        return StringUtils.isBlank(cronExpression) ? defaultCronExpression(): cronExpression;
    }

    public Set<EndpointRequest> getEndpointRequests() {
        return CollectionUtils.isEmpty(endpointRequests) ? new HashSet<>(): Collections.unmodifiableSet(endpointRequests);
    }

    @JsonIgnore
    public Long getPk() {
        return pk;
    }

}
