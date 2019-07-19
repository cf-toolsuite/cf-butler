package io.pivotal.cfapp.service;

import java.time.LocalDate;

import org.cloudfoundry.reactor.DefaultConnectionContext;
import org.cloudfoundry.reactor.TokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;

import io.pivotal.cfapp.config.PasSettings;
import io.pivotal.cfapp.domain.accounting.application.AppUsageReport;
import io.pivotal.cfapp.domain.accounting.service.ServiceUsageReport;
import io.pivotal.cfapp.domain.accounting.task.TaskUsageReport;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

// @see https://docs.pivotal.io/pivotalcf/2-4/opsguide/accounting-report.html

@Service
public class UsageService {

    private final OrganizationService orgService;
    private final WebClient webClient;
    private final DefaultConnectionContext connectionContext;
    private final TokenProvider tokenProvider;
    private final PasSettings settings;

    @Autowired
    public UsageService(
        OrganizationService orgService,
        WebClient webClient,
        DefaultConnectionContext connectionContext,
        TokenProvider tokenProvider,
        PasSettings settings,
        UsageCache cache) {
        this.orgService = orgService;
        this.webClient = webClient;
        this.connectionContext = connectionContext;
        this.tokenProvider = tokenProvider;
        this.settings = settings;
    }

    // FIXME Refactor Mono<String> JSON-like output to domain objects so we can start to drive aggregate calculations

    private Mono<String> getUsage(String usageType, String orgGuid, LocalDate start, LocalDate end) {
        Assert.hasText(orgGuid, "Global unique identifier for organization must not be blank or null!");
        Assert.notNull(start, "Start of date range must be specified!");
        Assert.notNull(end, "End of date range must be specified!");
        Assert.isTrue(end.isAfter(start), "Date range is invalid!");
        String uri = settings.getUsageDomain() + "/organizations/{orgGuid}/{usageType}?start={start}&end={end}";
        return getOauthToken()
                .flatMap(t -> webClient
                                .get()
                                    .uri(uri, orgGuid, usageType, start.toString(), end.toString())
                                    .header(HttpHeaders.AUTHORIZATION, t)
                                        .retrieve()
                                            .bodyToMono(String.class));
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
        return orgService
                .findAll()
                    .flatMap(o -> getTaskUsage(o.getId(), start, end));
    }

    public Flux<String> getApplicationUsage(LocalDate start, LocalDate end) {
        return orgService
                .findAll()
                .flatMap(o -> getApplicationUsage(o.getId(), start, end));
    }

    public Flux<String> getServiceUsage(LocalDate start, LocalDate end) {
        return orgService
                .findAll()
                    .flatMap(o -> getServiceUsage(o.getId(), start, end));
    }

    //----------

    public Mono<TaskUsageReport> getTaskReport() {
        String uri = settings.getUsageDomain() + "/system_report/task_usages";
        return getOauthToken()
                .flatMap(t -> webClient
                                .get()
                                    .uri(uri)
                                    .header(HttpHeaders.AUTHORIZATION, t)
                                        .retrieve()
                                            .bodyToMono(TaskUsageReport.class));
            }

    public Mono<AppUsageReport> getApplicationReport() {
        String uri = settings.getUsageDomain() + "/system_report/app_usages";
        return getOauthToken()
                .flatMap(t -> webClient
                                .get()
                                    .uri(uri)
                                    .header(HttpHeaders.AUTHORIZATION, t)
                                        .retrieve()
                                            .bodyToMono(AppUsageReport.class));
    }

    public Mono<ServiceUsageReport> getServiceReport() {
        String uri = settings.getUsageDomain() + "/system_report/service_usages";
        return getOauthToken()
                .flatMap(t -> webClient
                                .get()
                                    .uri(uri)
                                    .header(HttpHeaders.AUTHORIZATION, t)
                                        .retrieve()
                                            .bodyToMono(ServiceUsageReport.class));
            }

    private Mono<String> getOauthToken() {
        tokenProvider.invalidate(connectionContext);
        return tokenProvider.getToken(connectionContext);
    }
}