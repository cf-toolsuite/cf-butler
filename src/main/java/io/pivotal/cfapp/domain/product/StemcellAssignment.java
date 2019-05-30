package io.pivotal.cfapp.domain.product;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;

@Builder
@Getter
@JsonPropertyOrder({
    "available_stemcell_versions",
    "deployed_product_version",
    "deployed_stemcell_version",
    "guid",
    "identifier",
    "is_staged_for_deletion",
    "label",
    "required_stemcell_os",
    "required_stemcell_version",
    "staged_product_version",
    "staged_stemcell_version",
})
public class StemcellAssignment {

    @Default
    @JsonProperty("available_stemcell_versions")
    private List<String> availableStemcellVersions = new ArrayList<>();

    @JsonProperty("deployed_product_version")
    private String deployedProductVersion;

    @JsonProperty("deployed_stemcell_version")
    private String deployedStemcellVersion;

    @JsonProperty("guid")
    private String guid;

    @JsonProperty("identifier")
    private String identifier;

    @JsonProperty("is_staged_for_deletion")
    private Boolean isStagedForDeletion;

    @JsonProperty("label")
    private String label;

    @JsonProperty("required_stemcell_os")
    private String requiredStemcellOs;

    @JsonProperty("required_stemcell_version")
    private String requiredStemcellVersion;

    @JsonProperty("staged_product_version")
    private String stagedProductVersion;

    @JsonProperty("staged_stemcell_version")
    private String stagedStemcellVersion;

    @JsonCreator
    public StemcellAssignment(
        @JsonProperty("available_stemcell_versions") List<String> availableStemcellVersions,
        @JsonProperty("deployed_product_version") String deployedProductVersion,
        @JsonProperty("deployed_stemcell_version") String deployedStemcellVersion,
        @JsonProperty("guid") String guid,
        @JsonProperty("identifier") String identifier,
        @JsonProperty("is_staged_for_deletion") Boolean isStagedForDeletion,
        @JsonProperty("label") String label,
        @JsonProperty("required_stemcell_os") String requiredStemcellOs,
        @JsonProperty("required_stemcell_version") String requiredStemcellVersion,
        @JsonProperty("staged_product_version") String stagedProductVersion,
        @JsonProperty("staged_stemcell_version") String stagedStemcellVersion
    ) {
        this.availableStemcellVersions = availableStemcellVersions;
        this.deployedProductVersion = deployedProductVersion;
        this.deployedStemcellVersion = deployedStemcellVersion;
        this.guid = guid;
        this.identifier = identifier;
        this.isStagedForDeletion = isStagedForDeletion;
        this.label = label;
        this.requiredStemcellOs = requiredStemcellOs;
        this.requiredStemcellVersion = requiredStemcellVersion;
        this.stagedProductVersion = stagedProductVersion;
        this.stagedStemcellVersion = stagedStemcellVersion;
    }

}