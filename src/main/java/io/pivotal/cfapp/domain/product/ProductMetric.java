package io.pivotal.cfapp.domain.product;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder
@Getter
@JsonPropertyOrder({
    "name", "type", "currently-installed-release-date", "currently-installed-version",
    "latest-available-release-date", "latest-available-version", "end-of-support-date",
    "days-behind-latest-available-version", "days-out-of-support", "end-of-life", "pre-release"
})
@EqualsAndHashCode
public class ProductMetric {

    public static String headers() {
        return String.join("name", "type", "currently-installed-release-date", "currently-installed-version",
                "latest-available-release-date", "latest-available-version", "end-of-support-date",
                "days-behind-latest-available-version", "days-out-of-support", "end-of-life", "pre-release");
    }

    private static String wrap(String value) {
        return value != null ? StringUtils.wrap(value, '"') : StringUtils.wrap("", '"');
    }

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
            @JsonProperty("name") String name,
            @JsonProperty("type") ProductType type,
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
    public Long getDaysBehindLatestAvailableVersion() {
        Long result = null;
        if (currentlyInstalledReleaseDate != null && latestAvailableReleaseDate != null) {
            result = ChronoUnit.DAYS.between(currentlyInstalledReleaseDate, latestAvailableReleaseDate);
        }
        return result;
    }

    @JsonProperty("days-out-of-support")
    public Long getDaysOutOfSupport() {
        Long result = null;
        if (isEndOfLife()) {
            result = ChronoUnit.DAYS.between(endOfSupportDate, LocalDate.now());
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

    public String toCsv() {
        return String.join(",", wrap(getName()), wrap(getType().getId()),
                wrap(getCurrentlyInstalledReleaseDate() != null ? getCurrentlyInstalledReleaseDate().toString(): ""),
                wrap(getCurrentlyInstalledVersion()), wrap(getLatestAvailableReleaseDate() != null ? getLatestAvailableReleaseDate().toString(): ""),
                wrap(getLatestAvailableVersion()), wrap(getEndOfSupportDate() != null ? getEndOfSupportDate().toString(): ""),
                wrap(getDaysBehindLatestAvailableVersion() != null ? String.valueOf(getDaysBehindLatestAvailableVersion()): ""),
                wrap(getDaysOutOfSupport() != null ? String.valueOf(getDaysOutOfSupport()): ""),
                wrap(String.valueOf(isEndOfLife())), wrap(String.valueOf(isPreRelease()))
                );
    }

}
