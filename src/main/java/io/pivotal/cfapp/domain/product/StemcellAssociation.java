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
    "guid",
    "identifier",
    "label",
    "staged_product_version",
    "deployed_product_version",
    "is_staged_for_deletion",
    "staged_stemcells",
    "deployed_stemcells",
    "available_stemcells",
    "required_stemcells"
})
public class StemcellAssociation {

    @JsonProperty("guid")
    private String guid;

    @JsonProperty("identifier")
    private String identifier;

    @JsonProperty("label")
    private String label;

    @JsonProperty("staged_product_version")
    private String stagedProductVersion;

    @JsonProperty("deployed_product_version")
    private String deployedProductVersion;

    @JsonProperty("is_staged_for_deletion")
    private Boolean isStagedForDeletion;

    @Default
    @JsonProperty("staged_stemcells")
    private List<StemcellDetail> stagedStemcells = new ArrayList<>();

    @Default
    @JsonProperty("deployed_stemcells")
    private List<StemcellDetail> deployedStemcells = new ArrayList<>();

    @Default
    @JsonProperty("available_stemcells")
    private List<StemcellDetail> availableStemcells = new ArrayList<>();

    @Default
    @JsonProperty("required_stemcells")
    private List<StemcellDetail> requiredStemcells = new ArrayList<>();

    @JsonCreator
    public StemcellAssociation(
        @JsonProperty("guid") String guid,
        @JsonProperty("identifier") String identifier,
        @JsonProperty("label") String label,
        @JsonProperty("staged_product_version") String stagedProductVersion,
        @JsonProperty("deployed_product_version") String deployedProductVersion,
        @JsonProperty("is_staged_for_deletion") Boolean isStagedForDeletion,
        @JsonProperty("staged_stemcells") List<StemcellDetail> stagedStemcells,
        @JsonProperty("deployed_stemcells") List<StemcellDetail> deployedStemcells,
        @JsonProperty("available_stemcells") List<StemcellDetail> availableStemcells,
        @JsonProperty("required_stemcells") List<StemcellDetail> requiredStemcells
    ) {
        this.guid = guid;
        this.identifier = identifier;
        this.label = label;
        this.stagedProductVersion = stagedProductVersion;
        this.deployedProductVersion = deployedProductVersion;
        this.isStagedForDeletion = isStagedForDeletion;
        this.stagedStemcells = stagedStemcells;
        this.deployedStemcells = deployedStemcells;
        this.availableStemcells = availableStemcells;
        this.requiredStemcells = requiredStemcells;
    }

}