package org.cftoolsuite.cfapp.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.cftoolsuite.cfapp.client.GitClient;
import org.cftoolsuite.cfapp.config.GitSettings;
import org.cftoolsuite.cfapp.domain.ApplicationPolicy;
import org.cftoolsuite.cfapp.domain.EndpointPolicy;
import org.cftoolsuite.cfapp.domain.HygienePolicy;
import org.cftoolsuite.cfapp.domain.LegacyPolicy;
import org.cftoolsuite.cfapp.domain.Policies;
import org.cftoolsuite.cfapp.domain.PoliciesValidator;
import org.cftoolsuite.cfapp.domain.QueryPolicy;
import org.cftoolsuite.cfapp.domain.ResourceNotificationPolicy;
import org.cftoolsuite.cfapp.domain.ServiceInstancePolicy;
import org.cftoolsuite.cfapp.event.PoliciesLoadedEvent;
import org.cftoolsuite.cfapp.event.StacksRetrievedEvent;
import org.cftoolsuite.cfapp.service.PoliciesService;
import org.eclipse.jgit.lib.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(
        prefix = "cf.policies.git", name = "uri"
        )
public class PoliciesLoader implements ApplicationListener<StacksRetrievedEvent> {

    private static final String APPLICATION_POLICY_SUFFIX = "-AP.json";
    private static final String SERVICE_INSTANCE_POLICY_SUFFIX = "-SIP.json";
    private static final String ENDPOINT_POLICY_SUFFIX = "-EP.json";
    private static final String QUERY_POLICY_SUFFIX = "-QP.json";
    private static final String HYGIENE_POLICY_SUFFIX = "-HP.json";
    private static final String RESOURCE_NOTIFICATION_POLICY_SUFFIX = "-RNP.json";
    private static final String LEGACY_POLICY_SUFFIX = "-LP.json";

    private final GitClient client;
    private final PoliciesService service;
    private final GitSettings settings;
    private final PoliciesValidator validator;
    private final ObjectMapper mapper;
    private ApplicationEventPublisher publisher;

    @Autowired
    public PoliciesLoader(
        GitClient client,
        PoliciesService service,
        GitSettings settings,
        PoliciesValidator validator,
        ObjectMapper mapper,
        ApplicationEventPublisher publisher
    ) {
        this.client = client;
        this.service = service;
        this.settings = settings;
        this.validator = validator;
        this.mapper = mapper;
        this.publisher = publisher;
    }

    public void load() {
        log.info("PoliciesLoader started");
        Repository repo = client.getRepository(settings);
        if (repo != null) {
            String uri = settings.getUri();
            List<ApplicationPolicy> applicationPolicies = new ArrayList<>();
            List<ServiceInstancePolicy> serviceInstancePolicies = new ArrayList<>();
            List<EndpointPolicy> endpointPolicies = new ArrayList<>();
            List<QueryPolicy> queryPolicies = new ArrayList<>();
            List<HygienePolicy> hygienePolicies = new ArrayList<>();
            List<ResourceNotificationPolicy> resourceNotificationPolicies = new ArrayList<>();
            List<LegacyPolicy> legacyPolicies = new ArrayList<>();
            String commit = client.orLatestCommit(settings.getCommit(), repo);
            log.info("-- Fetching policies from {} using commit {}", uri, commit);
            settings
            .getFilePaths()
            .stream()
            .filter(fp -> !fp.startsWith("#"))
            .forEach(fp -> {
                String fileContent;
                try {
                    fileContent = client.readFile(repo, commit, fp);
                    if (fp.endsWith(APPLICATION_POLICY_SUFFIX)) {
                        ApplicationPolicy policy = mapper.readValue(fileContent, ApplicationPolicy.class);
                        if (validator.validate(policy)) {
                            applicationPolicies.add(policy);
                        }
                    } else if (fp.endsWith(SERVICE_INSTANCE_POLICY_SUFFIX)) {
                        ServiceInstancePolicy policy = mapper.readValue(fileContent, ServiceInstancePolicy.class);
                        if (validator.validate(policy)) {
                            serviceInstancePolicies.add(policy);
                        }
                    } else if (fp.endsWith(ENDPOINT_POLICY_SUFFIX)) {
                        EndpointPolicy policy = mapper.readValue(fileContent, EndpointPolicy.class);
                        if (validator.validate(policy)) {
                            endpointPolicies.add(policy);
                        }
                    } else if (fp.endsWith(QUERY_POLICY_SUFFIX)) {
                        QueryPolicy policy = mapper.readValue(fileContent, QueryPolicy.class);
                        if (validator.validate(policy)) {
                            queryPolicies.add(policy);
                        }
                    } else if (fp.endsWith(HYGIENE_POLICY_SUFFIX)) {
                        HygienePolicy policy = mapper.readValue(fileContent, HygienePolicy.class);
                        if (validator.validate(policy)) {
                            hygienePolicies.add(policy);
                        }
                    } else if (fp.endsWith(RESOURCE_NOTIFICATION_POLICY_SUFFIX)) {
                        ResourceNotificationPolicy policy = mapper.readValue(fileContent, ResourceNotificationPolicy.class);
                        if (validator.validate(policy)) {
                            resourceNotificationPolicies.add(policy);
                        }
                    } else if (fp.endsWith(LEGACY_POLICY_SUFFIX)) {
                        LegacyPolicy policy = mapper.readValue(fileContent, LegacyPolicy.class);
                        if (validator.validate(policy)) {
                            legacyPolicies.add(policy);
                        }
                    } else {
                        log.warn(
                                "Policy file {} does not adhere to naming convention. File name must end with one of {}.",
                                fp, List.of(APPLICATION_POLICY_SUFFIX, SERVICE_INSTANCE_POLICY_SUFFIX, QUERY_POLICY_SUFFIX, HYGIENE_POLICY_SUFFIX, RESOURCE_NOTIFICATION_POLICY_SUFFIX, LEGACY_POLICY_SUFFIX));
                    }
                } catch (IOException e1) {
                    log.warn("Could not read {} from {} with commit {} ", fp, uri, commit);
                }
            });
            service
            .deleteAll()
            .then(service.save(
                    Policies
                    .builder()
                    .applicationPolicies(applicationPolicies)
                    .serviceInstancePolicies(serviceInstancePolicies)
                    .endpointPolicies(endpointPolicies)
                    .queryPolicies(queryPolicies)
                    .hygienePolicies(hygienePolicies)
                    .resourceNotificationPolicies(resourceNotificationPolicies)
                    .legacyPolicies(legacyPolicies)
                    .build()
                    ))
            .then(service.findAll())
            .subscribe(
                    result -> {
                        publisher.publishEvent(new PoliciesLoadedEvent(this).policies(result));
                        log.info("PoliciesLoader completed");
                        log.info(
                                String.format("-- Loaded %d application policies, %d service instance policies, %d endpoint policies, %d query policies, %d hygiene policies, %d resource notification policies, and %d legacy policies.",
                                        result.getApplicationPolicies().size(),
                                        result.getServiceInstancePolicies().size(),
                                        result.getEndpointPolicies().size(),
                                        result.getQueryPolicies().size(),
                                        result.getHygienePolicies().size(),
                                        result.getResourceNotificationPolicies().size(),
                                        result.getLegacyPolicies().size()
                                        )
                                );
                    },
                    error -> {
                        log.error("PoliciesLoader terminated with error", error);
                    }
                    );
        } else {
            log.error("PoliciesLoader terminated because it could not connect to Git repository");
        }
    }

    @Override
    public void onApplicationEvent(StacksRetrievedEvent event) {
        load();
    }

}
