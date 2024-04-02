package org.cftoolsuite.cfapp.domain;

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
@JsonPropertyOrder({ "id", "stacks", "service-offerings", "operator-email-template", "notifyee-email-template", "organization-whitelist" })
@Getter
@Table("legacy_policy")
public class LegacyPolicy implements HasOrganizationWhiteList {

    public static LegacyPolicy seed(LegacyPolicy policy) {
        return LegacyPolicy
                .builder()
                .stacks(policy.getStacks())
                .serviceOfferings(policy.getServiceOfferings())
                .operatorTemplate(policy.getOperatorTemplate())
                .notifyeeTemplate(policy.getNotifyeeTemplate())
                .organizationWhiteList(policy.getOrganizationWhiteList())
                .build();
    }

    public static LegacyPolicy seedWith(LegacyPolicy policy, String id) {
        return LegacyPolicy
                .builder()
                .id(id)
                .stacks(policy.getStacks())
                .serviceOfferings(policy.getServiceOfferings())
                .operatorTemplate(policy.getOperatorTemplate())
                .notifyeeTemplate(policy.getNotifyeeTemplate())
                .organizationWhiteList(policy.getOrganizationWhiteList())
                .build();
    }

    @Id
    @JsonIgnore
    private Long pk;

    @Default
    @JsonProperty("id")
    private String id = Generators.timeBasedGenerator().generate().toString();

    @Default
    @JsonProperty("stacks")
    private Set<String> stacks = new HashSet<>();

    @Default
    @JsonProperty("service-offerings")
    private Set<String> serviceOfferings = new HashSet<>();

    @JsonProperty("operator-email-template")
    @Column("operator_email_template")
    private EmailNotificationTemplate operatorTemplate;

    @JsonProperty("notifyee-email-template")
    @Column("notifyee_email_template")
    private EmailNotificationTemplate notifyeeTemplate;

    @Default
    @JsonProperty("organization-whitelist")
    @Column("organization_whitelist")
    private Set<String> organizationWhiteList = new HashSet<>();

    @JsonCreator
    public LegacyPolicy(
            @JsonProperty("pk") Long pk,
            @JsonProperty("id") String id,
            @JsonProperty("stacks") Set<String> stacks,
            @JsonProperty("service-offerings") Set<String> serviceOfferings,
            @JsonProperty("operator-email-template") EmailNotificationTemplate operatorTemplate,
            @JsonProperty("notifyee-email-template") EmailNotificationTemplate notifyeeTemplate,
            @JsonProperty("organization-whitelist") Set<String> organizationWhiteList
            ) {
        this.pk = pk;
        this.id = id;
        this.stacks = stacks;
        this.serviceOfferings = serviceOfferings;
        this.operatorTemplate = operatorTemplate;
        this.notifyeeTemplate = notifyeeTemplate;
        this.organizationWhiteList = organizationWhiteList;
    }

    public Set<String> getOrganizationWhiteList() {
        return CollectionUtils.isEmpty(organizationWhiteList) ? new HashSet<>() : Collections.unmodifiableSet(organizationWhiteList);
    }

    @JsonIgnore
    public Long getPk() {
        return pk;
    }

    public Set<String> getServiceOfferings() {
        return CollectionUtils.isEmpty(serviceOfferings) ? new HashSet<>() : Collections.unmodifiableSet(serviceOfferings);
    }

    public Set<String> getStacks() {
        return CollectionUtils.isEmpty(stacks) ? new HashSet<>() : Collections.unmodifiableSet(stacks);
    }
}
