package io.pivotal.cfapp.service;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;

import io.pivotal.cfapp.config.ButlerSettings;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

// @see https://docs.pivotal.io/pivotalcf/2-4/opsguide/accounting-report.html

@Service
public class UsageService {

    private final OrganizationService orgService;
    private final WebClient webClient;
    private final ButlerSettings settings;

    @Autowired
    public UsageService(
        OrganizationService orgService,
        WebClient webClient,
        ButlerSettings settings) {
        this.orgService = orgService;
        this.webClient = webClient;
        this.settings = settings;
    }

    // FIXME Refactor Mono<String> JSON-like output to domain objects so we can start to drive aggregate calculations

    private Mono<String> getUsage(String usageType, String orgGuid, LocalDate start, LocalDate end) {
        Assert.hasText(orgGuid, "Global unique identifier for organization must not be blank or null!");
        Assert.notNull(start, "Start of date range must be specified!");
        Assert.notNull(end, "End of date range must be specified!");
        Assert.isTrue(end.isAfter(start), "Date range is invalid!");
        String uri = settings.getUsageDomain() + "/organizations/{orgGuid}/{usageType}?start={start}&end={end}";
        return webClient
                .get()
                    .uri(uri, orgGuid, usageType, start.toString(), end.toString())
                    .header(HttpHeaders.AUTHORIZATION, settings.getOauthToken())
                        .retrieve()
                            .bodyToMono(String.class);
    }

    private Mono<String> getSystemReport(String usageType) {
        String uri = settings.getUsageDomain() + "/system_report/{usageType}";
        return webClient
                .get()
                    .uri(uri, usageType)
                    .header(HttpHeaders.AUTHORIZATION, settings.getOauthToken())
                        .retrieve()
                            .bodyToMono(String.class);
    }

    public Mono<String> getTaskUsage(String orgGuid, LocalDate start, LocalDate end) {
        return getUsage("task_usages", orgGuid, start, end);
    }

    public Mono<String> getApplicationUsage(String orgGuid, LocalDate start, LocalDate end) {
        return getUsage("app_usages", orgGuid, start, end);
    }

    public Mono<String> getServiceUsage(String orgGuid, LocalDate start, LocalDate end) {
        return getUsage("service_usages", orgGuid, start, end);
    }

    public Flux<String> getTaskUsage(LocalDate start, LocalDate end) {
        return orgService.getOrganizations().flatMap(o -> getTaskUsage(o.getId(), start, end));
    }

    public Flux<String> getApplicationUsage(LocalDate start, LocalDate end) {
        return orgService.getOrganizations().flatMap(o -> getApplicationUsage(o.getId(), start, end));
    }

    public Flux<String> getServiceUsage(LocalDate start, LocalDate end) {
        return orgService.getOrganizations().flatMap(o -> getServiceUsage(o.getId(), start, end));
    }

    public Mono<String> getTaskReport() {
        return getSystemReport("task_usages");
    }

    public Mono<String> getApplicationReport() {
        return getSystemReport("app_usages");
    }

    public Mono<String> getServiceReport() {
        return getSystemReport("service_usages");
    }
}