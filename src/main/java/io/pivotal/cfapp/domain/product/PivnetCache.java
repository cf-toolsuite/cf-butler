package io.pivotal.cfapp.domain.product;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Component
@Slf4j
public class PivnetCache {

    private Products products;
    private List<Release> allProductReleases = new ArrayList<>();
    private List<Release> latestProductReleases = new ArrayList<>();

    public Release findLatestMinorProductReleaseBySlugAndVersion(String slug, String version) {
        List<Release> candidates =
            allProductReleases
                .stream()
                .filter(release ->
                    release.getSlug().equals(slug) && version.startsWith(release.getVersion().split("\\.")[0]))
                .sorted(Comparator.comparing(Release::getReleaseDate).reversed())
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(candidates)) {
            log.trace("Found latest minor release by {} and {}. \n\t {}", slug, version, candidates.get(0).toCsv());
            return candidates.get(0);
        } else {
            log.trace("No match found for latest minor release by {} and {}.", slug, version);
            return Release.empty();
        }
    }

    public Release findLatestProductReleaseBySlug(String slug) {
        List<Release> candidates =
            latestProductReleases
                .stream()
                .filter(release -> release.getSlug().equals(slug))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(candidates)) {
            log.trace("Found latest release by {}. \n\t {}", slug, candidates.get(0).toCsv());
            return candidates.get(0);
        } else {
            log.trace("No match found for latest release by {}.", slug);
            return Release.empty();
        }
    }

    public Release findProductReleaseBySlugAndVersion(String slug, String version) {
        List<Release> candidates =
            allProductReleases
                .stream()
                .filter(release ->
                    release.getSlug().equals(slug) &&
                        (release.getVersion().equals(version) || version.startsWith(release.getVersion())))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(candidates)) {
            log.trace("Found release by {} and {}. \n\t {}", slug, version, candidates.get(0).toCsv());
            return candidates.get(0);
        } else {
            log.trace("No match found for release by {} and {}.", slug, version);
            return Release.empty();
        }
    }

}
