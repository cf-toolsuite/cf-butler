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

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id", "git-commit", "resource-email-template", "resource-email-metadata", "resource-whitelist", "resource-blacklist", "cron-expression" })
@Getter
@Table("resource_notification_policy")
public class ResourceNotificationPolicy implements Policy {

    public static ResourceNotificationPolicy seed(ResourceNotificationPolicy policy) {
        return ResourceNotificationPolicy
                .builder()
                .resourceEmailTemplate(policy.getResourceEmailTemplate())
                .resourceEmailMetadata(policy.getResourceEmailMetadata())
                .resourceWhiteList(policy.getResourceWhiteList())
                .resourceBlackList(policy.getResourceBlackList())
                .cronExpression(policy.getCronExpression())
                .build();
    }

    public static ResourceNotificationPolicy seedWith(ResourceNotificationPolicy policy, String gitCommit) {
        return ResourceNotificationPolicy
                .builder()
                .gitCommit(gitCommit)
                .resourceEmailTemplate(policy.getResourceEmailTemplate())
                .resourceEmailMetadata(policy.getResourceEmailMetadata())
                .resourceWhiteList(policy.getResourceWhiteList())
                .resourceBlackList(policy.getResourceBlackList())
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

    @JsonProperty("resource-email-template")
    @Column("resource_email_template")
    private EmailNotificationTemplate resourceEmailTemplate;


    @JsonProperty("resource-email-metadata")
    @Column("resource_email_metadata")
    private ResourceEmailMetadata resourceEmailMetadata;

    @Default
    @JsonProperty("resource-whitelist")
    @Column("resource_whitelist")
    private Set<String> resourceWhiteList = new HashSet<>();

    @Default
    @JsonProperty("resource-blacklist")
    @Column("resource_blacklist")
    private Set<String> resourceBlackList = new HashSet<>();

    @JsonProperty("cron-expression")
    @Column("cron_expression")
    private String cronExpression;

    @JsonCreator
    public ResourceNotificationPolicy(
            @JsonProperty("pk") Long pk,
            @JsonProperty("id") String id,
            @JsonProperty("git-commit") String gitCommit,
            @JsonProperty("resource-email-template") EmailNotificationTemplate resourceEmailTemplate,
            @JsonProperty("resource-email-metadata") ResourceEmailMetadata resourceEmailMetadata,
            @JsonProperty("resource-whitelist") Set<String> resourceWhiteList,
            @JsonProperty("resource-blacklist") Set<String> resourceBlackList,
            @JsonProperty("cron-expression") String cronExpression
            ) {
        this.pk = pk;
        this.id = id;
        this.gitCommit = gitCommit;
        this.resourceEmailTemplate = resourceEmailTemplate;
        this.resourceEmailMetadata = resourceEmailMetadata;
        this.resourceWhiteList = resourceWhiteList;
        this.resourceBlackList = resourceBlackList;
        this.cronExpression = cronExpression;
    }

    public String getCronExpression() {
        return StringUtils.isBlank(cronExpression) ? defaultCronExpression(): cronExpression;
    }

    @JsonIgnore
    public Long getPk() {
        return pk;
    }

    public Set<String> getResourceWhiteList() {
        return CollectionUtils.isEmpty(resourceWhiteList) ? new HashSet<>() : Collections.unmodifiableSet(resourceWhiteList);
    }

    public Set<String> getResourceBlackList() {
        return CollectionUtils.isEmpty(resourceBlackList) ? new HashSet<>() : Collections.unmodifiableSet(resourceBlackList);
    }

}
