package io.pivotal.cfapp.domain;

import java.time.Duration;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.github.davidmoten.guavamini.Optional;

import lombok.Getter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "description", "state", "from-datetime", "from-duration", "unbind-services", "delete-services" })
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
	
	@JsonProperty("unbind-services")
	private boolean unbindServices;
	
	@JsonProperty("delete-services")
	private boolean deleteServices;

	@JsonCreator
	public ApplicationPolicy(@JsonProperty("description") String description, 
			@JsonProperty("state") String state, 
			@JsonProperty("from-datetime") LocalDateTime fromDateTime, 
			@JsonProperty("from-duration") Duration fromDuration,
			@JsonProperty("unbind-services") boolean unbindServices,
			@JsonProperty("delete-services") boolean deleteServices) {
		this.description = description;
		this.state = state;
		this.fromDateTime = fromDateTime;
		this.fromDuration = fromDuration;
		this.unbindServices = unbindServices;
		this.deleteServices = deleteServices;
	}
	
	@JsonIgnore
	public boolean isInvalid() {
		return !Optional.fromNullable(state).isPresent() ||
				Optional.fromNullable(fromDateTime).isPresent() 
				&& Optional.fromNullable(fromDuration).isPresent();
	}
	
}