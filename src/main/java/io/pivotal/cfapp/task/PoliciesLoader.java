package io.pivotal.cfapp.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.client.GitClient;
import io.pivotal.cfapp.config.PoliciesSettings;
import io.pivotal.cfapp.domain.ApplicationPolicy;
import io.pivotal.cfapp.domain.Policies;
import io.pivotal.cfapp.domain.ServiceInstancePolicy;
import io.pivotal.cfapp.service.PoliciesService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(name = "cf.policies.provider", havingValue = "git")
public class PoliciesLoader implements ApplicationListener<DatabaseCreatedEvent> {

	private static final String APPLICATION_POLICY_SUFFIX = "-AP.json";
	private static final String SERVICE_INSTANCE_POLICY_SUFFIX = "-SIP.json";

	private final GitClient client;
	private final PoliciesService service;
	private final PoliciesSettings settings;
	private final ObjectMapper mapper;

	@Autowired
	public PoliciesLoader(
			GitClient client,
			PoliciesService service,
			PoliciesSettings settings,
			ObjectMapper mapper
			) {
		this.client = client;
		this.service = service;
		this.settings = settings;
		this.mapper = mapper;
	}

	@Override
    public void onApplicationEvent(DatabaseCreatedEvent event) {
        load();
	}

	public void load() {
		log.info("PoliciesLoader started");
		try {
			Repository repo = client.getRepository(settings.getUri());
			List<ApplicationPolicy> applicationPolicies = new ArrayList<>();
			List<ServiceInstancePolicy> serviceInstancePolicies = new ArrayList<>();
			settings
				.getFilePaths()
					.forEach(fp -> {
						String fileContent;
						try {
							fileContent = client.readFile(repo, settings.getCommit(), fp);
							if (fp.endsWith(APPLICATION_POLICY_SUFFIX)) {
								applicationPolicies.add(mapper.readValue(fileContent, ApplicationPolicy.class));
							} else if (fp.endsWith(SERVICE_INSTANCE_POLICY_SUFFIX)) {
								serviceInstancePolicies.add(mapper.readValue(fileContent, ServiceInstancePolicy.class));
							} else {
								log.warn(
										"Policy file {} does not adhere to naming convention. File name must end with either {} or {}.",
										fp, APPLICATION_POLICY_SUFFIX, SERVICE_INSTANCE_POLICY_SUFFIX);
							}
						} catch (IOException ioe) {
							log.warn("Could not read {} from {} with commit {} ", fp, settings.getUri(), settings.getCommit());
						}
					});
			service
				.deleteAll()
				.then(service.save(new Policies(applicationPolicies, serviceInstancePolicies)))
				.subscribe(
					result -> {
						log.info("PoliciesLoader completed");
						log.info(String.format("-- Loaded %s application policies and %s service instance policies.", result.getApplicationPolicies().size(), result.getServiceInstancePolicies().size()));
					},
					error -> {
						log.error("PoliciesLoader terminated with error", error);
					}
				);
		} catch (GitAPIException | IOException ioe) {
			log.error("PoliciesLoader terminated with error", ioe);
		}
	}

}
