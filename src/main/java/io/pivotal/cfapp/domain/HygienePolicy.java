package io.pivotal.cfapp.domain;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.uuid.Generators;

import org.springframework.data.annotation.Id;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id", "days-since-last-update", "operator-email-template", "notifyee-email-template", "organization-whitelist" })
@Getter
public class HygienePolicy {

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
    private EmailNotificationTemplate operatorTemplate;

    @JsonProperty("notifyee-email-template")
    private EmailNotificationTemplate notifyeeTemplate;

	@Default
	@JsonProperty("organization-whitelist")
	private Set<String> organizationWhiteList = new HashSet<>();

    @JsonCreator
    public HygienePolicy(
        @JsonProperty("pk") Long pk,
		@JsonProperty("id") String id,
        @JsonProperty("days-since-last-update") Integer daysSinceLastUpdate,
        @JsonProperty("operator-email-template") EmailNotificationTemplate operatorTemplate,
		@JsonProperty("notifyee-email-template") EmailNotificationTemplate notifyeeTemplate,
		@JsonProperty("organization-whitelist") Set<String> organizationWhiteList
    ) {
        this.pk = pk;
		this.id = id;
        this.daysSinceLastUpdate = daysSinceLastUpdate;
        this.operatorTemplate = operatorTemplate;
		this.notifyeeTemplate = notifyeeTemplate;
		this.organizationWhiteList = organizationWhiteList;
    }

    @JsonIgnore
	public Long getPk() {
		return pk;
    }

    public static String tableName() {
		return "hygiene_policy";
	}

	public static String[] columnNames() {
		return
			new String[] {
				"pk", "id", "days_since_last_update", "operator_email_template", "notifyee_email_template", "organization_whitelist"
			};
	}

	public static HygienePolicy seed(HygienePolicy policy) {
		return HygienePolicy
				.builder()
					.daysSinceLastUpdate(policy.getDaysSinceLastUpdate())
					.operatorTemplate(policy.getOperatorTemplate())
					.notifyeeTemplate(policy.getNotifyeeTemplate())
					.organizationWhiteList(policy.getOrganizationWhiteList())
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
					.build();
	}
}