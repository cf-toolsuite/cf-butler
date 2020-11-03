package io.pivotal.cfapp.domain;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.jsonwebtoken.lang.Collections;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "resource", "labels", "email-domain" })
@Getter
public class ResourceEmailMetadata {

    @JsonProperty("resource")
    private String resource;

    @Default
    @JsonProperty("labels")
    private List<String> labels = new ArrayList<>();

    @JsonProperty("email-domain")
    private String emailDomain;


    @JsonCreator
    public ResourceEmailMetadata(
            @JsonProperty("resource") String resource,
            @JsonProperty("labels") List<String> labels,
            @JsonProperty("email-domain") String emailDomain
            ) {
        this.resource = resource;
        this.labels = labels;
        this.emailDomain = emailDomain;
    }

    @JsonIgnore
    public boolean isValid() {
        return isValidResource(resource)
                && !Collections.isEmpty(labels)
                && StringUtils.isNotBlank(emailDomain);
    }
    
    private static boolean isValidResource(String resource) {
        ResourceType[] resourceTypes = ResourceType.values();
        for (ResourceType resourceType : resourceTypes)
            if (resourceType.getId().equals(resource))
                return true;
        return false;
    }

}
