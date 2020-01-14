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
import lombok.ToString;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id", "description", "endpoints", "email-notification-template" })
@Getter
@ToString
public class EndpointPolicy {

	@Id
	@JsonIgnore
	private Long pk;

	@Default
	@JsonProperty("id")
	private String id = Generators.timeBasedGenerator().generate().toString();

	@JsonProperty("description")
	private String description;

	@Default
	@JsonProperty("endpoints")
	private Set<String> endpoints = new HashSet<>();

	@JsonProperty("email-notification-template")
	private EmailNotificationTemplate emailNotificationTemplate;

	@JsonCreator
	EndpointPolicy(
			@JsonProperty("pk") Long pk,
			@JsonProperty("id") String id,
			@JsonProperty("description") String description,
			@JsonProperty("endpoints") Set<String> endpoints,
			@JsonProperty("email-notification-template") EmailNotificationTemplate emailNotificationTemplate) {
		this.pk = pk;
		this.id = id;
		this.description = description;
		this.endpoints = endpoints;
		this.emailNotificationTemplate = emailNotificationTemplate;
	}

	@JsonIgnore
	public Long getPk() {
		return pk;
	}

	public Set<String> getEndpoints() {
		return CollectionUtils.isEmpty(endpoints) ? new HashSet<>(): Collections.unmodifiableSet(endpoints);
	}

	public static String tableName() {
		return "endpoint_policy";
	}

	public static String[] columnNames() {
		return
			new String[] {
				"pk", "id", "description", "endpoints", "email_notification_template"
			};
	}

	public static EndpointPolicy seed(EndpointPolicy policy) {
		return EndpointPolicy
				.builder()
					.description(policy.getDescription())
					.endpoints(policy.getEndpoints())
					.emailNotificationTemplate(policy.getEmailNotificationTemplate())
					.build();
	}

	public static EndpointPolicy seedWith(EndpointPolicy policy, String id) {
		return EndpointPolicy
				.builder()
					.id(id)
					.description(policy.getDescription())
					.endpoints(policy.getEndpoints())
					.emailNotificationTemplate(policy.getEmailNotificationTemplate())
					.build();
	}

}