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
@JsonPropertyOrder({
    "id", "days-since-last-update", "operator-email-template", "notifyee-email-template", "organization-whitelist", "include-applications", "include-service-instances"
})
@Getter
@Table("hygiene_policy")
public class HygienePolicy implements HasOrganizationWhiteList {

    public static HygienePolicy seed(HygienePolicy policy) {
        return HygienePolicy
                .builder()
                .daysSinceLastUpdate(policy.getDaysSinceLastUpdate())
                .operatorTemplate(policy.getOperatorTemplate())
                .notifyeeTemplate(policy.getNotifyeeTemplate())
                .organizationWhiteList(policy.getOrganizationWhiteList())
                .includeApplications(policy.isIncludeApplications())
                .includeServiceInstances(policy.isIncludeServiceInstances())
                .build();
    }

    public static HygienePolicy seedWith(HygienePolicy policy, String id) {
        return HygienePolicy
                .builder()
                .id(id)
                .daysSinceLastUpdate(policy.getDaysSinceLastUpdate())
                .operatorTemplate(policy.getOperatorTemplate())
                .notifyeeTemplate(policy.getNotifyeeTemplate())
                .organizationWhiteList(policy.getOrganizationWhiteList())
                .includeApplications(policy.isIncludeApplications())
                .includeServiceInstances(policy.isIncludeServiceInstances())
                .build();
    }

    @Id
    @JsonIgnore
    private Long pk;

    @Default
    @JsonProperty("id")
    private String id = Generators.timeBasedGenerator().generate().toString();

    @Default
    @JsonProperty("days-since-last-update")
    private Integer daysSinceLastUpdate = 180;

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

    @Default
    @JsonProperty("include-applications")
    @Column("include_applications")
    private boolean includeApplications = true;

    @Default
    @JsonProperty("include-service-instances")
    @Column("include_service_instances")
    private boolean includeServiceInstances = true;

    @JsonCreator
    public HygienePolicy(
            @JsonProperty("pk") Long pk,
            @JsonProperty("id") String id,
            @JsonProperty("days-since-last-update") Integer daysSinceLastUpdate,
            @JsonProperty("operator-email-template") EmailNotificationTemplate operatorTemplate,
            @JsonProperty("notifyee-email-template") EmailNotificationTemplate notifyeeTemplate,
            @JsonProperty("organization-whitelist") Set<String> organizationWhiteList,
            @JsonProperty("include-applications") boolean includeApplications,
            @JsonProperty("include-service-instances") boolean includeServiceInstances
            ) {
        this.pk = pk;
        this.id = id;
        this.daysSinceLastUpdate = daysSinceLastUpdate;
        this.operatorTemplate = operatorTemplate;
        this.notifyeeTemplate = notifyeeTemplate;
        this.organizationWhiteList = organizationWhiteList;
        this.includeApplications = includeApplications;
        this.includeServiceInstances = includeServiceInstances;
    }

    public Set<String> getOrganizationWhiteList() {
        return CollectionUtils.isEmpty(organizationWhiteList) ? new HashSet<>() : Collections.unmodifiableSet(organizationWhiteList);
    }

    @JsonIgnore
    public Long getPk() {
        return pk;
    }
}
