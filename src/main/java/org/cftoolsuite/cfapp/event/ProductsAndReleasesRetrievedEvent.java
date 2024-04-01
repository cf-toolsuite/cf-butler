package org.cftoolsuite.cfapp.event;

import java.util.List;

import org.cftoolsuite.cfapp.domain.product.Products;
import org.cftoolsuite.cfapp.domain.product.Release;
import org.springframework.context.ApplicationEvent;

public class ProductsAndReleasesRetrievedEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private Products products;
    private List<Release> allReleases;
    private List<Release> latestReleases;

    public ProductsAndReleasesRetrievedEvent(Object source) {
        super(source);
    }

    public ProductsAndReleasesRetrievedEvent allReleases(List<Release> allReleases) {
        this.allReleases = allReleases;
        return this;
    }

    public List<Release> getAllReleases() {
        return allReleases;
    }

    public List<Release> getLatestReleases() {
        return latestReleases;
    }

    public Products getProducts() {
        return products;
    }

    public ProductsAndReleasesRetrievedEvent latestReleases(List<Release> latestReleases) {
        this.latestReleases = latestReleases;
        return this;
    }

    public ProductsAndReleasesRetrievedEvent products(Products products) {
        this.products = products;
        return this;
    }

}
