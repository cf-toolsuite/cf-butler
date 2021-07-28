package io.pivotal.cfapp.task;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import io.pivotal.cfapp.config.PasSettings;
import io.pivotal.cfapp.domain.EmailAttachment;
import io.pivotal.cfapp.domain.EndpointPolicy;
import io.pivotal.cfapp.event.EmailNotificationEvent;
import io.pivotal.cfapp.service.PoliciesService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Slf4j
@Component
public class EndpointPolicyExecutorTask implements PolicyExecutorTask {

    private final PasSettings settings;
    private final PoliciesService policiesService;
    private final WebClient client;
    private final ApplicationEventPublisher publisher;
    private final Environment env;

    @Autowired
    public EndpointPolicyExecutorTask(
            PasSettings settings,
            PoliciesService policiesService,
            WebClient client,
            ApplicationEventPublisher publisher,
            Environment env
            ) {
        this.settings = settings;
        this.policiesService = policiesService;
        this.client = client;
        this.publisher = publisher;
        this.env = env;
    }

    private String determineName(String endpoint) {
        return endpoint
                .replaceFirst("/", "")
                .replace("/", "-")
                .replace("?q=", "-")
                .replace("?", "-")
                .replace("[]", "")
                .replace("=", "-equal-");
    }

    @Override
    public void execute() {
        log.info("EndpointPolicyExecutorTask started");
        fetchEndpointPolicies()
        .concatMap(ep -> exerciseEndpoints(ep).collectList().map(result -> Tuples.of(ep, result)))
        .collectList()
        .subscribe(
            results -> {
                results.forEach(
                    result ->
                        publisher.publishEvent(
                            new EmailNotificationEvent(this)
                                .domain(settings.getAppsDomain())
                                .from(result.getT1().getEmailNotificationTemplate().getFrom())
                                .recipients(result.getT1().getEmailNotificationTemplate().getTo())
                                .subject(result.getT1().getEmailNotificationTemplate().getSubject())
                                .body(result.getT1().getEmailNotificationTemplate().getBody())
                                .attachments(buildAttachments(result.getT2()))
                        )
                );
                log.info("EndpointPolicyExecutorTask completed");
                log.info("-- {} endpoint policies executed.", results.size());
            },
            error -> {
                log.error("EndpointPolicyExecutorTask terminated with error", error);
            }
        );
    }

    protected Mono<ResponseEntity<String>> exerciseEndpoint(String endpoint) {
        String[] routes = env.getProperty("vcap.application.uris", String[].class);
        String port = env.getProperty("local.server.port", String.class);
        String host = routes != null ? routes[0]: "localhost:" + port;
        String scheme = host.startsWith("localhost") ? "http://": "https://";
        String uri = scheme + host + endpoint;
        return client.get().uri(uri).retrieve().toEntity(String.class);
    }

    protected Flux<Tuple2<String, ResponseEntity<String>>> exerciseEndpoints(EndpointPolicy policy) {
        return Flux
                .fromIterable(policy.getEndpoints())
                .concatMap(e -> exerciseEndpoint(e).map(result -> Tuples.of(determineName(e), result)));
    }

    protected Flux<EndpointPolicy> fetchEndpointPolicies() {
        return
                policiesService
                .findAllEndpointPolicies()
                .flatMapMany(policy -> Flux.fromIterable(policy.getEndpointPolicies()));
    }

    @Scheduled(cron = "${cron.execution}")
    protected void runTask() {
        execute();
    }

    private static List<EmailAttachment> buildAttachments(List<Tuple2<String, ResponseEntity<String>>> tuples) {
        List<EmailAttachment> result = new ArrayList<>();
        for (Tuple2<String, ResponseEntity<String>> t: tuples) {
            String filename = t.getT1();
            String content = t.getT2().getBody();
            String mimeType = t.getT2().getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
            if (StringUtils.isNotBlank(content)) {
                if (mimeType.startsWith(MediaType.APPLICATION_JSON_VALUE)) {
                    result.add(
                            EmailAttachment
                            .builder()
                            .filename(filename)
                            .content(content)
                            .extension(".json")
                            .mimeType(mimeType)
                            .build()
                            );
                } else if (mimeType.startsWith(MediaType.TEXT_PLAIN_VALUE)) {
                    result.add(
                            EmailAttachment
                            .builder()
                            .filename(filename)
                            .content(content)
                            .extension(".txt")
                            .mimeType(mimeType)
                            .build()
                            );
                }
            } else {
                result.add(
                        EmailAttachment
                        .builder()
                        .filename(t.getT1())
                        .content("No results")
                        .extension(".txt")
                        .mimeType("text/plain")
                        .build()
                        );
            }
        }
        return result;
    }
}
