package io.pivotal.cfapp.domain;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.uuid.Generators;
import com.github.davidmoten.guavamini.Optional;

import lombok.Builder;
import lombok.Getter;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id", "description", "state", "from-datetime", "from-duration", 
	"delete-services", "organization-whitelist" })
@Getter
public class ApplicationPolicy {

	@Id
	@Builder.Default
	@JsonProperty("id")
	private String id = Generators.timeBasedGenerator().generate().toString();

	@JsonProperty("description")
	private String description;

	@JsonProperty("state")
	private String state;

	@JsonProperty("from-datetime")
	private LocalDateTime fromDateTime;

	@JsonProperty("from-duration")
	private Duration fromDuration;

	@JsonProperty("delete-services")
	private boolean deleteServices;

	@JsonProperty("organization-whitelist")
	private Set<String> organizationWhiteList;

	@JsonCreator
	ApplicationPolicy(
			@JsonProperty("id") String id,
			@JsonProperty("description") String description, 
			@JsonProperty("state") String state,
			@JsonProperty("from-datetime") LocalDateTime fromDateTime,
			@JsonProperty("from-duration") Duration fromDuration,
			@JsonProperty("delete-services") boolean deleteServices,
			@JsonProperty("organization-whitelist") Set<String> organizationWhiteList) {
		this.id = id;
		this.description = description;
		this.state = state;
		this.fromDateTime = fromDateTime;
		this.fromDuration = fromDuration;
		this.deleteServices = deleteServices;
		this.organizationWhiteList = organizationWhiteList;
	}

	@JsonIgnore
	public boolean isInvalid() {
		return Optional.fromNullable(id).isPresent() || !Optional.fromNullable(state).isPresent()
				|| (Optional.fromNullable(fromDateTime).isPresent() && Optional.fromNullable(fromDuration).isPresent())
				|| (!Optional.fromNullable(fromDateTime).isPresent()
						&& !Optional.fromNullable(fromDuration).isPresent());
	}

	public Set<String> getOrganizationWhiteList() {
		return CollectionUtils.isEmpty(organizationWhiteList) ? new HashSet<>() : organizationWhiteList;
	}
	
	public static ApplicationPolicy seed(ApplicationPolicy policy) {
		return ApplicationPolicy
				.builder()
					.description(policy.getDescription())
					.deleteServices(policy.isDeleteServices())
					.fromDateTime(policy.getFromDateTime())
					.fromDuration(policy.getFromDuration())
					.organizationWhiteList(policy.getOrganizationWhiteList())
					.state(policy.getState())
					.build();
	}
	
	public static ApplicationPolicy seedWith(ApplicationPolicy policy, String id) {
		return ApplicationPolicy
				.builder()
					.id(id)
					.description(policy.getDescription())
					.deleteServices(policy.isDeleteServices())
					.fromDateTime(policy.getFromDateTime())
					.fromDuration(policy.getFromDuration())
					.organizationWhiteList(policy.getOrganizationWhiteList())
					.state(policy.getState())
					.build();
	}

}