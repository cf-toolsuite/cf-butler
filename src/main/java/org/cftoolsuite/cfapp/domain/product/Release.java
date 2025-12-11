package org.cftoolsuite.cfapp.domain.product;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringUtils;
import org.cftoolsuite.cfapp.deser.RelaxedLocalDateDeserializer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import tools.jackson.databind.annotation.JsonDeserialize;

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
    "end_of_support_date",
    "end_of_guidance_date",
    "end_of_availability_date",
    "eccn",
    "license_exception",
    "updated_at",
    "software_files_updated_at",
    "_links"
})
public class Release {

    private static final String BASE_URL = "https://network.tanzu.vmware.com/api/v2/products/";

    public static Release empty() {
        return Release.builder().build();
    }

    public static String headers() {
        return String.join(",", "id", "slug", "version", "release_type", "release_date",
                "release_notes_url", "availability", "description", "end_of_support_date",
                "end_of_guidance_date", "end_of_availability_date", "eccn", "license_exception", "updated_at",
                "software_files_updated_at");
    }

    private static String wrap(String value) {
        return value != null ? StringUtils.wrap(value, '"') : StringUtils.wrap("", '"');
    }

    @Default
    @JsonProperty("id")
    private Long id = -1L;

    @JsonProperty("version")
    private String version;

    @JsonProperty("release_type")
    private String releaseType;

    @JsonDeserialize(using = RelaxedLocalDateDeserializer.class)
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

    @JsonDeserialize(using = RelaxedLocalDateDeserializer.class)
    @JsonProperty("end_of_support_date")
    private LocalDate endOfSupportDate;

    @JsonDeserialize(using = RelaxedLocalDateDeserializer.class)
    @JsonProperty("end_of_guidance_date")
    private LocalDate endOfGuidanceDate;

    @JsonDeserialize(using = RelaxedLocalDateDeserializer.class)
    @JsonProperty("end_of_availability_date")
    private LocalDate endOfAvailabilityDate;

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
            @JsonProperty("end_of_guidance_date") LocalDate endOfGuidanceDate,
            @JsonProperty("end_of_availability_date") LocalDate endOfAvailabilityDate,
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
        this.endOfGuidanceDate = endOfGuidanceDate;
        this.endOfAvailabilityDate = endOfAvailabilityDate;
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

    public String toCsv() {
        return String.join(",", wrap(String.valueOf(getId())), wrap(getSlug()), wrap(getVersion()),
                wrap(getReleaseType()), wrap(getReleaseDate() != null ? getReleaseDate().toString(): ""),
                wrap(getReleaseNotesUrl()), wrap(getAvailability()), wrap(getDescription()),
                wrap(getEndOfSupportDate() != null ? DateTimeFormatter.ISO_LOCAL_DATE.format(getEndOfSupportDate()): null),
                wrap(getEndOfGuidanceDate() != null ? DateTimeFormatter.ISO_LOCAL_DATE.format(getEndOfGuidanceDate()): null),
                wrap(getEndOfAvailabilityDate() != null ? DateTimeFormatter.ISO_LOCAL_DATE.format(getEndOfAvailabilityDate()): null),
                wrap(getEccn()), wrap(getLicenseException()), wrap(getUpdatedAt() != null ? getUpdatedAt().toString(): ""),
                wrap(getSoftwareFilesUpdatedAt() != null ? getSoftwareFilesUpdatedAt().toString(): ""));
    }

}
