
package org.cftoolsuite.cfapp.domain.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Builder;
import lombok.Getter;


@Builder
@Getter
@JsonPropertyOrder({
    "self",
    "eula_acceptance",
    "product_files",
    "file_groups",
    "user_groups",
    "artifact_references"
})
public class ReleaseLinks {

    @JsonProperty("self")
    private Self self;

    @JsonProperty("eula_acceptance")
    private EulaAcceptance eulaAcceptance;

    @JsonProperty("product_files")
    private ProductFiles productFiles;

    @JsonProperty("file_groups")
    private FileGroups fileGroups;

    @JsonProperty("user_groups")
    private UserGroups userGroups;

    @JsonProperty("artifact_references")
    private ArtifactReferences artifactReferences;

    public ReleaseLinks(
            @JsonProperty("self") Self self,
            @JsonProperty("eula_acceptance") EulaAcceptance eulaAcceptance,
            @JsonProperty("product_files") ProductFiles productFiles,
            @JsonProperty("file_groups") FileGroups fileGroups,
            @JsonProperty("user_groups") UserGroups userGroups,
            @JsonProperty("artifact_references") ArtifactReferences artifactReferences
            ) {
        this.self = self;
        this.eulaAcceptance = eulaAcceptance;
        this.productFiles = productFiles;
        this.fileGroups = fileGroups;
        this.userGroups = userGroups;
        this.artifactReferences = artifactReferences;
    }

}
