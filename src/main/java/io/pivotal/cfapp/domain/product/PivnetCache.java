package io.pivotal.cfapp.domain.product;

import java.util.List;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class PivnetCache {

    private Products products;
    private List<Release> allReleases;
    private List<Release> latestProductReleases;
}