package io.pivotal.cfapp.domain.product;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import lombok.Data;

@Data
@Component
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
            return candidates.get(0);
        } else {
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
            return candidates.get(0);
        } else {
            return Release.empty();
        }
    }

    public Release findProductReleaseBySlugAndVersion(String slug, String version) {
        List<Release> candidates =
            allProductReleases
                .stream()
                .filter(release -> release.getSlug().equals(slug) && release.getVersion().equals(version))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(candidates)) {
            return candidates.get(0);
        } else {
            return Release.empty();
        }
    }

}
