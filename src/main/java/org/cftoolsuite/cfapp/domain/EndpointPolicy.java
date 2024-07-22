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
@JsonPropertyOrder({ "id", "git-commit", "description", "endpoints", "email-notification-template", "cron-expression", "apply-json-to-csv-converter" })
@Getter
@ToString
@Table("endpoint_policy")
public class EndpointPolicy implements Policy {

    public static EndpointPolicy seed(EndpointPolicy policy) {
        return EndpointPolicy
                .builder()
                .applyJsonToCsvConverter(policy.isApplyJsonToCsvConverter())
                .cronExpression(policy.getCronExpression())
                .description(policy.getDescription())
                .endpoints(policy.getEndpoints())
                .emailNotificationTemplate(policy.getEmailNotificationTemplate())
                .build();
    }

    public static EndpointPolicy seedWith(EndpointPolicy policy, String gitCommit) {
        return EndpointPolicy
                .builder()
                .gitCommit(gitCommit)
                .applyJsonToCsvConverter(policy.isApplyJsonToCsvConverter())
                .cronExpression(policy.getCronExpression())
                .description(policy.getDescription())
                .endpoints(policy.getEndpoints())
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
    @JsonProperty("endpoints")
    private Set<String> endpoints = new HashSet<>();

    @JsonProperty("email-notification-template")
    private EmailNotificationTemplate emailNotificationTemplate;

    @JsonProperty("cron-expression")
    @Column("cron_expression")
    private String cronExpression;

    @JsonProperty("apply-json-to-csv-converter")
    @Column("apply_json_to_csv_converter")
    private boolean applyJsonToCsvConverter;

    @JsonCreator
    EndpointPolicy(
            @JsonProperty("pk") Long pk,
            @JsonProperty("id") String id,
            @JsonProperty("git-commit") String gitCommit,
            @JsonProperty("description") String description,
            @JsonProperty("endpoints") Set<String> endpoints,
            @JsonProperty("email-notification-template") EmailNotificationTemplate emailNotificationTemplate,
            @JsonProperty("cron-expression") String cronExpression,
            @JsonProperty("apply-json-to-csv-converter") boolean applyJsonToCsvConverter) {
        this.pk = pk;
        this.id = id;
        this.gitCommit = gitCommit;
        this.description = description;
        this.endpoints = endpoints;
        this.emailNotificationTemplate = emailNotificationTemplate;
        this.cronExpression = cronExpression;
        this.applyJsonToCsvConverter = applyJsonToCsvConverter;
    }

    public String getCronExpression() {
        return StringUtils.isBlank(cronExpression) ? defaultCronExpression(): cronExpression;
    }

    public Set<String> getEndpoints() {
        return CollectionUtils.isEmpty(endpoints) ? new HashSet<>(): Collections.unmodifiableSet(endpoints);
    }

    @JsonIgnore
    public Long getPk() {
        return pk;
    }
}
