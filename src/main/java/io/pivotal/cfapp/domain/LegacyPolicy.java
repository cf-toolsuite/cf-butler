package io.pivotal.cfapp.domain;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.uuid.Generators;

import org.springframework.data.annotation.Id;
import org.springframework.util.CollectionUtils;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id", "stacks", "operator-email-template", "notifyee-email-template", "organization-whitelist" })
@Getter
public class LegacyPolicy {

    @Id
	@JsonIgnore
	private Long pk;

	@Default
	@JsonProperty("id")
    private String id = Generators.timeBasedGenerator().generate().toString();

	@Default
	@JsonProperty("stacks")
	private Set<String> stacks = new HashSet<>();

    @JsonProperty("operator-email-template")
    private EmailNotificationTemplate operatorTemplate;

    @JsonProperty("notifyee-email-template")
    private EmailNotificationTemplate notifyeeTemplate;

	@Default
	@JsonProperty("organization-whitelist")
	private Set<String> organizationWhiteList = new HashSet<>();

    @JsonCreator
    public LegacyPolicy(
        @JsonProperty("pk") Long pk,
		@JsonProperty("id") String id,
		@JsonProperty("stacks") Set<String> stacks,
        @JsonProperty("operator-email-template") EmailNotificationTemplate operatorTemplate,
		@JsonProperty("notifyee-email-template") EmailNotificationTemplate notifyeeTemplate,
		@JsonProperty("organization-whitelist") Set<String> organizationWhiteList
    ) {
        this.pk = pk;
		this.id = id;
		this.stacks = stacks;
        this.operatorTemplate = operatorTemplate;
		this.notifyeeTemplate = notifyeeTemplate;
		this.organizationWhiteList = organizationWhiteList;
    }

    @JsonIgnore
	public Long getPk() {
		return pk;
    }

	public Set<String> getStacks() {
		return CollectionUtils.isEmpty(stacks) ? new HashSet<>() : Collections.unmodifiableSet(stacks);
	}

	public Set<String> getOrganizationWhiteList() {
		return CollectionUtils.isEmpty(organizationWhiteList) ? new HashSet<>() : Collections.unmodifiableSet(organizationWhiteList);
	}

    public static String tableName() {
		return "legacy_policy";
	}

	public static String[] columnNames() {
		return
			new String[] {
				"pk", "id", "stacks", "operator_email_template", "notifyee_email_template", "organization_whitelist"
			};
	}

	public static LegacyPolicy seed(LegacyPolicy policy) {
		return LegacyPolicy
				.builder()
					.stacks(policy.getStacks())
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
					.operatorTemplate(policy.getOperatorTemplate())
					.notifyeeTemplate(policy.getNotifyeeTemplate())
					.organizationWhiteList(policy.getOrganizationWhiteList())
					.build();
	}
}