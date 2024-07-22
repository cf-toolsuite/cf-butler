package org.cftoolsuite.cfapp.task;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.cftoolsuite.cfapp.config.PasSettings;
import org.cftoolsuite.cfapp.domain.EmailAttachment;
import org.cftoolsuite.cfapp.domain.EndpointPolicy;
import org.cftoolsuite.cfapp.event.EmailNotificationEvent;
import org.cftoolsuite.cfapp.service.PoliciesService;
import org.cftoolsuite.cfapp.util.JsonToCsvConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

@Slf4j
@Component
public class EndpointPolicyExecutorTask implements PolicyExecutorTask {

    private final PasSettings settings;
    private final PoliciesService policiesService;
    private final JsonToCsvConverter converter;
    private final WebClient client;
    private final ApplicationEventPublisher publisher;
    private final Environment env;

    @Autowired
    public EndpointPolicyExecutorTask(
            PasSettings settings,
            PoliciesService policiesService,
            JsonToCsvConverter converter,
            WebClient client,
            ApplicationEventPublisher publisher,
            Environment env) {
        this.settings = settings;
        this.policiesService = policiesService;
        this.converter = converter;
        this.client = client;
        this.publisher = publisher;
        this.env = env;
    }

    @Override
    public void execute(String id) {
        log.info("EndpointPolicyExecutorTask with id={} started", id);
        fetchEndpointPolicy(id)
            .concatMap(ep -> exerciseEndpoints(ep).collectList().map(result -> Tuples.of(ep, result)))
            .collectList()
            .subscribe(
                results -> handleSuccess(results, id),
                error -> log.error("EndpointPolicyExecutorTask with id={} terminated with error", id, error)
            );
    }

    private void handleSuccess(List<Tuple2<EndpointPolicy, List<Tuple3<String, Boolean, ResponseEntity<String>>>>> results, String id) {
        results.forEach(result -> {
            EndpointPolicy policy = result.getT1();
            List<Tuple3<String, Boolean, ResponseEntity<String>>> endpointResults = result.getT2();
            publisher.publishEvent(buildEmailNotificationEvent(policy, endpointResults));
        });
        log.info("EndpointPolicyExecutorTask with id={} completed", id);
    }

    private EmailNotificationEvent buildEmailNotificationEvent(EndpointPolicy policy, List<Tuple3<String, Boolean, ResponseEntity<String>>> endpointResults) {
        return new EmailNotificationEvent(this)
            .domain(settings.getAppsDomain())
            .from(policy.getEmailNotificationTemplate().getFrom())
            .recipients(policy.getEmailNotificationTemplate().getTo())
            .subject(policy.getEmailNotificationTemplate().getSubject())
            .body(policy.getEmailNotificationTemplate().getBody())
            .attachments(buildAttachments(endpointResults));
    }

    protected Mono<ResponseEntity<String>> exerciseEndpoint(String endpoint) {
        String[] routes = env.getProperty("vcap.application.uris", String[].class);
        String port = env.getProperty("local.server.port", String.class);
        String host = routes != null ? routes[0] : "localhost:" + port;
        String scheme = host.startsWith("localhost") ? "http://" : "https://";
        String uri = scheme + host + endpoint;
        return client.get().uri(uri).retrieve().toEntity(String.class);
    }

    protected Flux<Tuple3<String, Boolean, ResponseEntity<String>>> exerciseEndpoints(EndpointPolicy policy) {
        return Flux.fromIterable(policy.getEndpoints())
            .concatMap(endpoint -> exerciseEndpoint(endpoint)
                .map(result -> Tuples.of(formatEndpointName(endpoint), policy.isApplyJsonToCsvConverter(), result)));
    }

    protected Flux<EndpointPolicy> fetchEndpointPolicy(String id) {
        return policiesService.findEndpointPolicyById(id)
            .flatMapMany(policy -> Flux.fromIterable(policy.getEndpointPolicies()));
    }

    private String formatEndpointName(String endpoint) {
        return endpoint.replaceFirst("/", "")
            .replace("/", "-")
            .replace("?q=", "-")
            .replace("?", "-")
            .replace("[]", "")
            .replace("=", "-equal-");
    }

    private List<EmailAttachment> buildAttachments(List<Tuple3<String, Boolean, ResponseEntity<String>>> endpointResults) {
        List<EmailAttachment> attachments = new ArrayList<>();
        for (Tuple3<String, Boolean, ResponseEntity<String>> result : endpointResults) {
            String filename = result.getT1();
            String content = result.getT3().getBody();
            String mimeType = result.getT3().getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);

            if (StringUtils.isNotBlank(mimeType) && StringUtils.isNotBlank(content)) {
                attachments.add(createAttachment(filename, content, mimeType, result.getT2()));
            } else {
                attachments.add(createDefaultAttachment(filename));
            }
        }
        return attachments;
    }

    private EmailAttachment createAttachment(String filename, String content, String mimeType, boolean applyConverter) {
        try {
            if (mimeType.startsWith(MediaType.APPLICATION_JSON_VALUE) && applyConverter) {
                return EmailAttachment.builder()
                    .filename(filename)
                    .content(converter.convert(content))
                    .extension(".csv")
                    .mimeType(MediaType.TEXT_PLAIN_VALUE)
                    .build();
            } else {
                String extension = mimeType.startsWith(MediaType.APPLICATION_JSON_VALUE) ? ".json" : ".txt";
                return EmailAttachment.builder()
                    .filename(filename)
                    .content(content)
                    .extension(extension)
                    .mimeType(mimeType)
                    .build();
            }
        } catch (IllegalArgumentException | JsonProcessingException e) {
            return EmailAttachment.builder()
                .filename(filename)
                .content("Trouble processing results.\n" + ExceptionUtils.getStackTrace(e))
                .extension(".txt")
                .mimeType(MediaType.TEXT_PLAIN_VALUE)
                .build();
        }
    }

    private EmailAttachment createDefaultAttachment(String filename) {
        return EmailAttachment.builder()
            .filename(filename)
            .content("No results")
            .extension(".txt")
            .mimeType(MediaType.TEXT_PLAIN_VALUE)
            .build();
    }
}

