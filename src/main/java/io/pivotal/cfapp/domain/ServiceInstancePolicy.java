package io.pivotal.cfapp.domain;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
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
import lombok.Getter;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id", "description", "from-datetime", "from-duration", "organization-whitelist" })
@Getter
public class ServiceInstancePolicy {

	@Id
	private Long pk;

	@Builder.Default
	@JsonProperty("id")
	private String id = Generators.timeBasedGenerator().generate().toString();

	@JsonProperty("description")
	private String description;

	@JsonProperty("from-datetime")
	private LocalDateTime fromDateTime;

	@JsonProperty("from-duration")
	private Duration fromDuration;

	@JsonProperty("organization-whitelist")
	private Set<String> organizationWhiteList;

	@JsonCreator
	ServiceInstancePolicy(
			@JsonProperty("pk") Long pk,
			@JsonProperty("id") String id,
			@JsonProperty("description") String description,
			@JsonProperty("from-datetime") LocalDateTime fromDateTime,
			@JsonProperty("from-duration") Duration fromDuration,
			@JsonProperty("organization-whitelist") Set<String> organizationWhiteList) {
		this.pk = pk;
		this.id = id;
		this.description = description;
		this.fromDateTime = fromDateTime;
		this.fromDuration = fromDuration;
		this.organizationWhiteList = organizationWhiteList;
	}

	@JsonIgnore
	public boolean isInvalid() {
		return Optional.ofNullable(id).isPresent() ||
			(Optional.ofNullable(fromDateTime).isPresent() && Optional.ofNullable(fromDuration).isPresent())
			|| (!Optional.ofNullable(fromDateTime).isPresent() && !Optional.ofNullable(fromDuration).isPresent());
	}

	@JsonIgnore
	public Long getPk() {
		return pk;
	}

	public Set<String> getOrganizationWhiteList() {
		return CollectionUtils.isEmpty(organizationWhiteList) ? new HashSet<>() : organizationWhiteList;
	}

	public static ServiceInstancePolicy seed(ServiceInstancePolicy policy) {
		return ServiceInstancePolicy
				.builder()
					.description(policy.getDescription())
					.fromDateTime(policy.getFromDateTime())
					.fromDuration(policy.getFromDuration())
					.organizationWhiteList(policy.getOrganizationWhiteList())
					.build();
	}

	public static ServiceInstancePolicy seedWith(ServiceInstancePolicy policy, String id) {
		return ServiceInstancePolicy
				.builder()
					.id(id)
					.description(policy.getDescription())
					.fromDateTime(policy.getFromDateTime())
					.fromDuration(policy.getFromDuration())
					.organizationWhiteList(policy.getOrganizationWhiteList())
					.build();
	}
}