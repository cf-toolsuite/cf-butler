package io.pivotal.cfapp.task;

import java.util.List;

import org.springframework.context.ApplicationEvent;

import io.pivotal.cfapp.domain.product.Release;

public class LatestProductReleasesRetrievedEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private List<Release> latestReleases;

    public LatestProductReleasesRetrievedEvent(Object source) {
        super(source);
    }

    public LatestProductReleasesRetrievedEvent latestReleases(List<Release> latestReleases) {
        this.latestReleases = latestReleases;
        return this;
    }

    public List<Release> getLatestReleases() {
        return latestReleases;
    }

}
