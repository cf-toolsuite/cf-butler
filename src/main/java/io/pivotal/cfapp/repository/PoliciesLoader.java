package io.pivotal.cfapp.repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.lib.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.pivotal.cfapp.config.PoliciesSettings;
import io.pivotal.cfapp.domain.ApplicationPolicy;
import io.pivotal.cfapp.domain.Policies;
import io.pivotal.cfapp.domain.ServiceInstancePolicy;
import io.pivotal.cfapp.service.PoliciesService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(name = "cf.policies.provider", havingValue = "git")
@AutoConfigureAfter(DatabaseCreator.class)
public class PoliciesLoader implements ApplicationRunner {

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
	public void run(ApplicationArguments args) throws Exception {
		log.info("PoliciesLoader started");
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
	}

}
