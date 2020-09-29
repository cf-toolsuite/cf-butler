package io.pivotal.cfapp.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "request",
    "droplet_guid",
    "package_guid",
    "build_guid",
    "app_port",
    "process_type",
    "route_guid",
    "route_mapping_guid"
})
public class EntityMetadata {

    @JsonProperty("request")
    private Request request;

    @JsonProperty("droplet_guid")
    private String dropletGuid;

    @JsonProperty("package_guid")
    private String packageGuid;

    @JsonProperty("build_guid")
    private String buildGuid;

    @JsonProperty("app_port")
    private Integer appPort;

    @JsonProperty("process_type")
    private String processType;

    @JsonProperty("route_guid")
    private String routeGuid;

    @JsonProperty("route_mapping_guid")
    private String routeMappingGuid;

    @JsonCreator
    public EntityMetadata(
            @JsonProperty("request") Request request,
            @JsonProperty("droplet_guid") String dropletGuid,
            @JsonProperty("package_guid") String packageGuid,
            @JsonProperty("build_guid") String buildGuid,
            @JsonProperty("app_port") Integer appPort,
            @JsonProperty("process_type") String processType,
            @JsonProperty("route_guid") String routeGuid,
            @JsonProperty("route_mapping_guid") String routeMappingGuid) {
        this.request = request;
        this.dropletGuid = dropletGuid;
        this.packageGuid = packageGuid;
        this.buildGuid = buildGuid;
        this.appPort = appPort;
        this.processType = processType;
        this.routeGuid = routeGuid;
        this.routeMappingGuid = routeMappingGuid;
    }

}
