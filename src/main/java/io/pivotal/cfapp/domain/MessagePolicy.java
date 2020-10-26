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
@JsonPropertyOrder({ "id", "owner-email-template", "organization-whitelist" })
@Getter
@Table("message_policy")
public class MessagePolicy {

    public static MessagePolicy seed(MessagePolicy policy) {
        return MessagePolicy
                .builder()
                .ownerTemplate(policy.getOwnerTemplate())
                .organizationWhiteList(policy.getOrganizationWhiteList())
                .build();
    }

    public static MessagePolicy seedWith(MessagePolicy policy, String id) {
        return MessagePolicy
                .builder()
                .id(id)
                .ownerTemplate(policy.getOwnerTemplate())
                .organizationWhiteList(policy.getOrganizationWhiteList())
                .build();
    }

    @Id
    @JsonIgnore
    private Long pk;

    @Default
    @JsonProperty("id")
    private String id = Generators.timeBasedGenerator().generate().toString();

    @JsonProperty("owner-email-template")
    @Column("owner_email_template")
    private EmailNotificationTemplate ownerTemplate;

    @Default
    @JsonProperty("organization-whitelist")
    @Column("organization_whitelist")
    private Set<String> organizationWhiteList = new HashSet<>();

    @JsonCreator
    public MessagePolicy(
            @JsonProperty("pk") Long pk,
            @JsonProperty("id") String id,
            @JsonProperty("owner-email-template") EmailNotificationTemplate ownerTemplate,
            @JsonProperty("organization-whitelist") Set<String> organizationWhiteList
            ) {
        this.pk = pk;
        this.id = id;
        this.ownerTemplate = ownerTemplate;
        this.organizationWhiteList = organizationWhiteList;
    }

    public Set<String> getOrganizationWhiteList() {
        return CollectionUtils.isEmpty(organizationWhiteList) ? new HashSet<>() : Collections.unmodifiableSet(organizationWhiteList);
    }

    @JsonIgnore
    public Long getPk() {
        return pk;
    }
}
