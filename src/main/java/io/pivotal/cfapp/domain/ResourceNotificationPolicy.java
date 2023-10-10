package io.pivotal.cfapp.domain;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
@JsonPropertyOrder({ "id", "resource-email-template", "resource-email-metadata", "resource-whitelist", "resource-blacklist" })
@Getter
@Table("resource_notification_policy")
public class ResourceNotificationPolicy {

    public static ResourceNotificationPolicy seed(ResourceNotificationPolicy policy) {
        return ResourceNotificationPolicy
                .builder()
                .resourceEmailTemplate(policy.getResourceEmailTemplate())
                .resourceEmailMetadata(policy.getResourceEmailMetadata())
                .resourceWhiteList(policy.getResourceWhiteList())
                .resourceBlackList(policy.getResourceBlackList())
                .build();
    }

    public static ResourceNotificationPolicy seedWith(ResourceNotificationPolicy policy, String id) {
        return ResourceNotificationPolicy
                .builder()
                .id(id)
                .resourceEmailTemplate(policy.getResourceEmailTemplate())
                .resourceEmailMetadata(policy.getResourceEmailMetadata())
                .resourceWhiteList(policy.getResourceWhiteList())
                .resourceBlackList(policy.getResourceBlackList())
                .build();
    }

    @Id
    @JsonIgnore
    private Long pk;

    @Default
    @JsonProperty("id")
    private String id = Generators.timeBasedGenerator().generate().toString();

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

    @JsonCreator
    public ResourceNotificationPolicy(
            @JsonProperty("pk") Long pk,
            @JsonProperty("id") String id,
            @JsonProperty("resource-email-template") EmailNotificationTemplate resourceEmailTemplate,
            @JsonProperty("resource-email-metadata") ResourceEmailMetadata resourceEmailMetadata,
            @JsonProperty("resource-whitelist") Set<String> resourceWhiteList,
            @JsonProperty("resource-blacklist") Set<String> resourceBlackList
            ) {
        this.pk = pk;
        this.id = id;
        this.resourceEmailTemplate = resourceEmailTemplate;
        this.resourceEmailMetadata = resourceEmailMetadata;
        this.resourceWhiteList = resourceWhiteList;
        this.resourceBlackList = resourceBlackList;
    }

    public Set<String> getResourceWhiteList() {
        return CollectionUtils.isEmpty(resourceWhiteList) ? new HashSet<>() : Collections.unmodifiableSet(resourceWhiteList);
    }

    public Set<String> getResourceBlackList() {
        return CollectionUtils.isEmpty(resourceBlackList) ? new HashSet<>() : Collections.unmodifiableSet(resourceBlackList);
    }

    @JsonIgnore
    public Long getPk() {
        return pk;
    }
}
