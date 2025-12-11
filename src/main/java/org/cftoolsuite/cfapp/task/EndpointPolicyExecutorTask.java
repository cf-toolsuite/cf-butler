package org.cftoolsuite.cfapp.task;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.cftoolsuite.cfapp.config.PasSettings;
import org.cftoolsuite.cfapp.domain.EmailAttachment;
import org.cftoolsuite.cfapp.domain.EndpointPolicy;
import org.cftoolsuite.cfapp.domain.EndpointRequest;
import org.cftoolsuite.cfapp.event.EmailNotificationEvent;
import org.cftoolsuite.cfapp.service.PoliciesService;
import org.cftoolsuite.cfapp.util.JsonToCsvConverter;
import org.cftoolsuite.cfapp.util.SimpleJsonPathAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
public class EndpointPolicyExecutorTask implements PolicyExecutorTask {

    private final PasSettings settings;
    private final PoliciesService policiesService;
    private final JsonToCsvConverter converter;
    private final WebClient client;
    private final ApplicationEventPublisher publisher;
    private final Environment env;
    private final SimpleJsonPathAdapter jsonPathAdapter;

    @Autowired
    public EndpointPolicyExecutorTask(
            PasSettings settings,
            PoliciesService policiesService,
            JsonToCsvConverter converter,
            WebClient client,
            ApplicationEventPublisher publisher,
            Environment env,
            ObjectMapper mapper) {
        this.settings = settings;
        this.policiesService = policiesService;
        this.jsonPathAdapter = new SimpleJsonPathAdapter(mapper);
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

    private void handleSuccess(List<Tuple2<EndpointPolicy, List<Tuple2<EndpointRequest, ResponseEntity<String>>>>> results, String id) {
        results.forEach(result -> {
            EndpointPolicy policy = result.getT1();
            List<Tuple2<EndpointRequest, ResponseEntity<String>>> endpointResults = result.getT2();
            publisher.publishEvent(buildEmailNotificationEvent(policy, endpointResults));
        });
        log.info("EndpointPolicyExecutorTask with id={} completed", id);
    }

    private EmailNotificationEvent buildEmailNotificationEvent(EndpointPolicy policy, List<Tuple2<EndpointRequest, ResponseEntity<String>>> endpointResults) {
        return new EmailNotificationEvent(this)
            .domain(settings.getAppsDomain())
            .from(policy.getEmailNotificationTemplate().getFrom())
            .recipients(policy.getEmailNotificationTemplate().getTo())
            .carbonCopyRecipients(policy.getEmailNotificationTemplate().getCc())
            .blindCarbonCopyRecipients(policy.getEmailNotificationTemplate().getBcc())
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

    protected Flux<Tuple2<EndpointRequest, ResponseEntity<String>>> exerciseEndpoints(EndpointPolicy policy) {
        return
            Flux
                .fromIterable(policy.getEndpointRequests())
                .flatMap(request ->
                    exerciseEndpoint(request.getEndpoint())
                        .map(response -> Tuples.of(request, response))
                );
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

    private String determineFilename(EndpointRequest request) {
        String result = "";
        if (StringUtils.isNotBlank(request.getJsonPathExpression())) {
            result =
                request
                    .getJsonPathExpression()
                    .replaceAll("\\s+|[\\p{Punct}&&[^._]]|[._]", "-")
                    .replaceAll("-+", "-")
                    .replaceAll("^-+", "")
                    .replaceAll("-+$", "");
        } else {
            result = formatEndpointName(request.getEndpoint());
        }
        return result;
    }

    private List<EmailAttachment> buildAttachments(List<Tuple2<EndpointRequest, ResponseEntity<String>>> endpointResults) {
        List<EmailAttachment> attachments = new ArrayList<>();
        for (Tuple2<EndpointRequest, ResponseEntity<String>> result : endpointResults) {
            String filename = determineFilename(result.getT1());
            String jsonPathExpression = result.getT1().getJsonPathExpression();
            boolean applyConverter = result.getT1().isApplyJsonToCsvConverter();
            String content = result.getT2().getBody();
            String mimeType = result.getT2().getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);

            if (StringUtils.isNotBlank(mimeType) && StringUtils.isNotBlank(content)) {
                attachments.add(createAttachment(filename, content, mimeType, jsonPathExpression, applyConverter));
            } else {
                attachments.add(createDefaultAttachment(filename));
            }
        }
        return attachments;
    }

    private EmailAttachment createAttachment(String filename, String data, String mimeType, String jsonPathExpression, boolean applyConverter) {
        log.info("Attempting to create an email attachment named {} with mimetype {}", filename, mimeType);
        log.trace("-- containing {}", data);
        String content = data;
        if (mimeType.startsWith(MediaType.APPLICATION_JSON_VALUE) && StringUtils.isNotBlank(jsonPathExpression)) {
            try {
                log.info("Attempting to extract fragment using JsonPath expression {}", jsonPathExpression);
                content = jsonPathAdapter.extractFragment(data, jsonPathExpression);
            } catch (Exception e) {
                throw new IllegalArgumentException("Could not extract fragment using expression: " + jsonPathExpression, e);
            }
        }
        try {
            if (mimeType.startsWith(MediaType.APPLICATION_JSON_VALUE) && applyConverter) {
                log.info("Attempting to convert JSON to CSV.");
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
        } catch (IllegalArgumentException | JacksonException e) {
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
