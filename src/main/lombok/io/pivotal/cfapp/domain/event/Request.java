package io.pivotal.cfapp.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "instances",
    "state",
    "related_guid",
    "relation",
    "route",
    "verb",
    "buildpack",
    "command",
    "console",
    "docker_credentials",
    "environment_json",
    "health_check_type",
    "memory",
    "name",
    "production",
    "space_guid",
    "stack_guid"
})
public class Request {

    @JsonProperty("instances")
    private Integer instances;

    @JsonProperty("state")
    private String state;

    @JsonProperty("related_guid")
    private String relatedGuid;

    @JsonProperty("relation")
    private String relation;

    @JsonProperty("route")
    private String route;

    @JsonProperty("verb")
    private String verb;

    @JsonProperty("buildpack")
    private String buildpack;

    @JsonProperty("command")
    private String command;

    @JsonProperty("console")
    private Boolean console;

    @JsonProperty("docker_credentials")
    private String dockerCredentials;

    @JsonProperty("environment_json")
    private String environmentJson;

    @JsonProperty("health_check_type")
    private String healthCheckType;

    @JsonProperty("memory")
    private Integer memory;

    @JsonProperty("name")
    private String name;

    @JsonProperty("production")
    private Boolean production;

    @JsonProperty("space_guid")
    private String spaceGuid;

    @JsonProperty("stack_guid")
    private String stackGuid;

    @JsonCreator
    public Request(
            @JsonProperty("instances") Integer instances,
            @JsonProperty("state") String state,
            @JsonProperty("related_guid") String relatedGuid,
            @JsonProperty("relation") String relation,
            @JsonProperty("route") String route,
            @JsonProperty("verb") String verb,
            @JsonProperty("buildpack") String buildpack,
            @JsonProperty("command") String command,
            @JsonProperty("console") Boolean console,
            @JsonProperty("docker_credentials") String dockerCredentials,
            @JsonProperty("environment_json") String environmentJson,
            @JsonProperty("health_check_type") String healthCheckType,
            @JsonProperty("memory") Integer memory,
            @JsonProperty("name") String name,
            @JsonProperty("production") Boolean production,
            @JsonProperty("space_guid") String spaceGuid,
            @JsonProperty("stack_guid") String stackGuid) {
        this.instances = instances;
        this.state = state;
        this.relatedGuid = relatedGuid;
        this.relation = relation;
        this.route = route;
        this.verb = verb;
        this.buildpack = buildpack;
        this.command = command;
        this.console = console;
        this.dockerCredentials = dockerCredentials;
        this.environmentJson = environmentJson;
        this.healthCheckType = healthCheckType;
        this.memory = memory;
        this.name = name;
        this.production = production;
        this.spaceGuid = spaceGuid;
        this.stackGuid = stackGuid;
    }

}
