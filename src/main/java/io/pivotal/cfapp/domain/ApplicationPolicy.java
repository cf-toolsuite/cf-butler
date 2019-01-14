package io.pivotal.cfapp.domain;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.github.davidmoten.guavamini.Optional;

import io.jsonwebtoken.lang.Collections;
import lombok.Getter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "description", "state", "from-datetime", "from-duration", "delete-services", "organization-whitelist" })
@Getter
public class ApplicationPolicy {
	
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
	private List<String> organizationWhiteList;

	@JsonCreator
	public ApplicationPolicy(@JsonProperty("description") String description, 
			@JsonProperty("state") String state, 
			@JsonProperty("from-datetime") LocalDateTime fromDateTime, 
			@JsonProperty("from-duration") Duration fromDuration,
			@JsonProperty("delete-services") boolean deleteServices,
			@JsonProperty("organization-whitelist") List<String> organizationWhiteList) {
		this.description = description;
		this.state = state;
		this.fromDateTime = fromDateTime;
		this.fromDuration = fromDuration;
		this.deleteServices = deleteServices;
		this.organizationWhiteList = organizationWhiteList;
	}
	
	@JsonIgnore
	public boolean isInvalid() {
		return !Optional.fromNullable(state).isPresent() ||
				(Optional.fromNullable(fromDateTime).isPresent() 
				&& Optional.fromNullable(fromDuration).isPresent()) ||
				(!Optional.fromNullable(fromDateTime).isPresent() 
						&& !Optional.fromNullable(fromDuration).isPresent());
	}
	
	@JsonIgnore
	public boolean whiteListExists() {
		return !Collections.isEmpty(organizationWhiteList);
	}
	
}