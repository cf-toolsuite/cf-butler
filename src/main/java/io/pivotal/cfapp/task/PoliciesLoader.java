package io.pivotal.cfapp.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.eclipse.jgit.lib.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.client.GitClient;
import io.pivotal.cfapp.config.PoliciesSettings;
import io.pivotal.cfapp.domain.ApplicationPolicy;
import io.pivotal.cfapp.domain.HygienePolicy;
import io.pivotal.cfapp.domain.Policies;
import io.pivotal.cfapp.domain.PoliciesValidator;
import io.pivotal.cfapp.domain.QueryPolicy;
import io.pivotal.cfapp.domain.ServiceInstancePolicy;
import io.pivotal.cfapp.event.StacksRetrievedEvent;
import io.pivotal.cfapp.service.PoliciesService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(name = "cf.policies.provider", havingValue = "git")
public class PoliciesLoader implements ApplicationListener<StacksRetrievedEvent> {

	private static final String APPLICATION_POLICY_SUFFIX = "-AP.json";
	private static final String SERVICE_INSTANCE_POLICY_SUFFIX = "-SIP.json";
	private static final String QUERY_POLICY_SUFFIX = "-QP.json";
	private static final String HYGIENE_POLICY_SUFFIX = "-HP.json";

	private final GitClient client;
	private final PoliciesService service;
	private final PoliciesSettings settings;
	private final PoliciesValidator validator;
	private final ObjectMapper mapper;

	@Autowired
	public PoliciesLoader(
			GitClient client,
			PoliciesService service,
			PoliciesSettings settings,
			PoliciesValidator validator,
			ObjectMapper mapper
			) {
		this.client = client;
		this.service = service;
		this.settings = settings;
		this.validator = validator;
		this.mapper = mapper;
	}

	@Override
	public void onApplicationEvent(StacksRetrievedEvent event) {
		load();
	}

	public void load() {
		log.info("PoliciesLoader started");
		Repository repo = client.getRepository(settings.getUri());
		if (repo != null) {
			List<ApplicationPolicy> applicationPolicies = new ArrayList<>();
			List<ServiceInstancePolicy> serviceInstancePolicies = new ArrayList<>();
			List<QueryPolicy> queryPolicies = new ArrayList<>();
			List<HygienePolicy> hygienePolicies = new ArrayList<>();
			String commit = client.orLatestCommit(settings.getCommit(), repo);
			log.info("-- Fetching policies from {} using commit {}", settings.getUri(), commit);
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
							} else {
								log.warn(
										"Policy file {} does not adhere to naming convention. File name must end with either {} or {}.",
										fp, APPLICATION_POLICY_SUFFIX, SERVICE_INSTANCE_POLICY_SUFFIX);
							}
						} catch (IOException e1) {
							log.warn("Could not read {} from {} with commit {} ", fp, settings.getUri(), settings.getCommit());
						}
					});
			service
				.deleteAll()
				.then(service.save(new Policies(applicationPolicies, serviceInstancePolicies, queryPolicies, hygienePolicies)))
				.subscribe(
					result -> {
						log.info("PoliciesLoader completed");
						log.info(
							String.format("-- Loaded %d application policies, %d service instance policies, %d query policies, and %d hygiene policies.",
								result.getApplicationPolicies().size(),
								result.getServiceInstancePolicies().size(),
								result.getQueryPolicies().size(),
								result.getHygienePolicies().size()
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

}
