package io.pivotal.cfapp.domain.product;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.nimbusds.oauth2.sdk.util.CollectionUtils;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class PivnetCache {

    private Products products;
    private List<Release> allProductReleases = new ArrayList<>();
    private List<Release> latestProductReleases = new ArrayList<>();

    public Release findProductReleaseBySlugAndVersion(String slug, String version) {
        List<Release> candidate =
            allProductReleases
                .stream()
                .filter(release -> release.getSlug().equals(slug) && release.getVersion().equals(version))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(candidate)) {
            return candidate.get(0);
        } else {
            return Release.empty();
        }
    }

    public Release findLatestProductReleaseBySlug(String slug) {
        List<Release> candidate =
            latestProductReleases
                .stream()
                .filter(release -> release.getSlug().equals(slug))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(candidate)) {
            return candidate.get(0);
        } else {
            return Release.empty();
        }
    }

}