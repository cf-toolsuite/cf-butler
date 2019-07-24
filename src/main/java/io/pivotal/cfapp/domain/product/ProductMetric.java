package io.pivotal.cfapp.domain.product;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@JsonPropertyOrder({
    "name", "type", "currently-installed-release-date", "currently-installed-version",
    "latest-available-release-date", "latest-available-version", "end-of-support-date",
    "days-behind-latest-available-version", "end-of-life", "pre-release"
})
public class ProductMetric {

    @JsonProperty("name")
    private String name;

    @JsonProperty("type")
    private ProductType type;

    @JsonProperty("currently-installed-release-date")
    private LocalDate currentlyInstalledReleaseDate;

    @JsonProperty("currently-installed-version")
    private String currentlyInstalledVersion;

    @JsonProperty("latest-available-release-date")
    private LocalDate latestAvailableReleaseDate;

    @JsonProperty("latest-available-version")
    private String latestAvailableVersion;

    @JsonProperty("end-of-support-date")
    private LocalDate endOfSupportDate;

    @JsonCreator
    public ProductMetric(
        @JsonProperty("name") String name, @JsonProperty("type") ProductType type,
        @JsonProperty("currently-installed-release-date") LocalDate currentlyInstalledReleaseDate,
        @JsonProperty("currently-installed-version") String currentlyInstalledVersion,
        @JsonProperty("latest-available-release-date") LocalDate latestAvailableReleaseDate,
        @JsonProperty("latest-available-version") String latestAvailableVersion,
        @JsonProperty("end-of-support-date") LocalDate endOfSupportDate
    ) {
        this.name = name;
        this.type = type;
        this.currentlyInstalledReleaseDate = currentlyInstalledReleaseDate;
        this.currentlyInstalledVersion = currentlyInstalledVersion;
        this.latestAvailableReleaseDate = latestAvailableReleaseDate;
        this.latestAvailableVersion = latestAvailableVersion;
        this.endOfSupportDate = endOfSupportDate;
    }

    @JsonProperty("days-behind-latest-available-version")
    public Long getStaleness() {
        Long result = null;
        if (currentlyInstalledReleaseDate != null && latestAvailableReleaseDate != null) {
            result = ChronoUnit.DAYS.between(currentlyInstalledReleaseDate, latestAvailableReleaseDate);
        }
        return result;
    }

    @JsonProperty("end-of-life")
    public boolean isEndOfLife() {
        boolean result = false;
        LocalDate today = LocalDate.now();
        if (endOfSupportDate != null && today.isAfter(endOfSupportDate)) {
            result = true;
        }
        return result;
    }

    @JsonProperty("pre-release")
    public boolean isPreRelease() {
        boolean result = false;
        if (currentlyInstalledReleaseDate == null || latestAvailableReleaseDate == null) {
            result = true;
        }
        return result;
    }

}