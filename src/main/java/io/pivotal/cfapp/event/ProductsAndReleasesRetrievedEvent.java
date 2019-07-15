package io.pivotal.cfapp.event;

import java.util.List;

import org.springframework.context.ApplicationEvent;

import io.pivotal.cfapp.domain.product.Products;
import io.pivotal.cfapp.domain.product.Release;

public class ProductsAndReleasesRetrievedEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private Products products;
    private List<Release> allReleases;
    private List<Release> latestReleases;

    public ProductsAndReleasesRetrievedEvent(Object source) {
        super(source);
    }

    public ProductsAndReleasesRetrievedEvent products(Products products) {
        this.products = products;
        return this;
    }

    public ProductsAndReleasesRetrievedEvent allReleases(List<Release> allReleases) {
        this.allReleases = allReleases;
        return this;
    }

    public ProductsAndReleasesRetrievedEvent latestReleases(List<Release> latestReleases) {
        this.latestReleases = latestReleases;
        return this;
    }

    public Products getProducts() {
        return products;
    }

    public List<Release> getAllReleases() {
        return allReleases;
    }

    public List<Release> getLatestReleases() {
        return latestReleases;
    }

}
