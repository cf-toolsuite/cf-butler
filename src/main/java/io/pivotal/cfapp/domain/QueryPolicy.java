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
@JsonPropertyOrder({ "id", "description", "queries", "email-notification-template" })
@Getter
@ToString
public class QueryPolicy {

	@Id
	@JsonIgnore
	private Long pk;

	@Default
	@JsonProperty("id")
	private String id = Generators.timeBasedGenerator().generate().toString();

	@JsonProperty("description")
	private String description;

	@Default
	@JsonProperty("queries")
	private Set<Query> queries = new HashSet<>();

	@JsonProperty("email-notification-template")
	private EmailNotificationTemplate emailNotificationTemplate;

	@JsonCreator
	QueryPolicy(
			@JsonProperty("pk") Long pk,
			@JsonProperty("id") String id,
			@JsonProperty("description") String description,
			@JsonProperty("queries") Set<Query> queries,
			@JsonProperty("email-notification-template") EmailNotificationTemplate emailNotificationTemplate) {
		this.pk = pk;
		this.id = id;
		this.description = description;
		this.queries = queries;
		this.emailNotificationTemplate = emailNotificationTemplate;
	}

	@JsonIgnore
	public Long getPk() {
		return pk;
	}

	public Set<Query> getQueries() {
		return CollectionUtils.isEmpty(queries) ? new HashSet<>(): Collections.unmodifiableSet(queries);
	}

	public static String tableName() {
		return "query_policy";
	}

	public static String[] columnNames() {
		return
			new String[] {
				"pk", "id", "description", "queries", "email_notification_template"
			};
	}

	public static QueryPolicy seed(QueryPolicy policy) {
		return QueryPolicy
				.builder()
					.description(policy.getDescription())
					.queries(policy.getQueries())
					.emailNotificationTemplate(policy.getEmailNotificationTemplate())
					.build();
	}

	public static QueryPolicy seedWith(QueryPolicy policy, String id) {
		return QueryPolicy
				.builder()
					.id(id)
					.description(policy.getDescription())
					.queries(policy.getQueries())
					.emailNotificationTemplate(policy.getEmailNotificationTemplate())
					.build();
	}

}