package io.pivotal.cfapp.domain.product;

import java.time.Instant;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Builder;
import lombok.Getter;
import lombok.Builder.Default;

@Builder
@Getter
@JsonPropertyOrder({
"id",
"slug",
"version",
"release_type",
"release_date",
"release_notes_url",
"availability",
"description",
"eula",
"eccn",
"license_exception",
"updated_at",
"software_files_updated_at",
"_links"
})
public class Release {

    private static final String BASE_URL = "https://network.pivotal.io/api/v2/products/";

    @Default
    @JsonProperty("id")
    private Long id = -1L;

    @JsonProperty("version")
    private String version;

    @JsonProperty("release_type")
    private String releaseType;

    @JsonProperty("release_date")
    private LocalDate releaseDate;

    @JsonProperty("release_notes_url")
    private String releaseNotesUrl;

    @JsonProperty("availability")
    private String availability;

    @JsonProperty("description")
    private String description;

    @JsonProperty("eula")
    private Eula eula;

    @JsonProperty("end_of_support_date")
    private LocalDate endOfSupportDate;

    @JsonProperty("eccn")
    private String eccn;

    @JsonProperty("license_exception")
    private String licenseException;

    @JsonProperty("updated_at")
    private Instant updatedAt;

    @JsonProperty("software_files_updated_at")
    private Instant softwareFilesUpdatedAt;

    @JsonProperty("_links")
    private ReleaseLinks links;

    @JsonCreator
    public Release(
        @JsonProperty("id") Long id,
        @JsonProperty("version") String version,
        @JsonProperty("release_type") String releaseType,
        @JsonProperty("release_date") LocalDate releaseDate,
        @JsonProperty("release_notes_url") String releaseNotesUrl,
        @JsonProperty("availability") String availability,
        @JsonProperty("description") String description,
        @JsonProperty("eula") Eula eula,
        @JsonProperty("end_of_support_date") LocalDate endOfSupportDate,
        @JsonProperty("eccn") String eccn,
        @JsonProperty("license_exception") String licenseException,
        @JsonProperty("updated_at") Instant updatedAt,
        @JsonProperty("software_files_updated_at") Instant softwareFilesUpdatedAt,
        @JsonProperty("_links") ReleaseLinks links
    ) {
        this.id = id;
        this.version = version;
        this.releaseType = releaseType;
        this.releaseDate = releaseDate;
        this.releaseNotesUrl = releaseNotesUrl;
        this.availability = availability;
        this.description = description;
        this.eula = eula;
        this.endOfSupportDate = endOfSupportDate;
        this.eccn = eccn;
        this.licenseException = licenseException;
        this.updatedAt = updatedAt;
        this.softwareFilesUpdatedAt = softwareFilesUpdatedAt;
        this.links = links;
    }

    @JsonProperty("slug")
    public String getSlug() {
        String ref = getLinks().getSelf().getHref();
        String refStrippedOfBaseUrl = ref.replace(BASE_URL, "");
        String slug = refStrippedOfBaseUrl.split("/")[0];
        return slug;
    }

}