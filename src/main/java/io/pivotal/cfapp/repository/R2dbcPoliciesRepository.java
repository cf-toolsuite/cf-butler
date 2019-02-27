package io.pivotal.cfapp.repository;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.function.DatabaseClient;
import org.springframework.stereotype.Repository;

import io.pivotal.cfapp.config.ButlerSettings.PoliciesSettings;
import io.pivotal.cfapp.domain.ApplicationPolicy;
import io.pivotal.cfapp.domain.Policies;
import io.pivotal.cfapp.domain.ServiceInstancePolicy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class R2dbcPoliciesRepository {

	private final DatabaseClient client;
	private final PoliciesSettings settings;

	@Autowired
	public R2dbcPoliciesRepository(
			DatabaseClient client,
			PoliciesSettings settings) {
		this.client = client;
		this.settings = settings;
	}

	public Mono<Policies> save(Policies entity) {
		List<ApplicationPolicy> applicationPolicies = entity.getApplicationPolicies()
				.stream()
				.filter(ap -> !ap.isInvalid())
				.map(p -> seedApplicationPolicy(p))
				.collect(Collectors.toList());

		List<ServiceInstancePolicy> serviceInstancePolicies = entity.getServiceInstancePolicies()
				.stream()
				.filter(sip -> !sip.isInvalid())
				.map(p -> seedServiceInstancePolicy(p))
				.collect(Collectors.toList());

		return Flux.fromIterable(applicationPolicies)
				.flatMap(ap -> client.insert().into("application_policy")
									.value("id", ap.getId())
									.value("description", ap.getDescription())
									.value("state", ap.getState())
									.value("from_datetime", ap.getFromDateTime() != null ? Timestamp.valueOf(ap.getFromDateTime()): null)
									.value("from_duration", ap.getFromDuration() != null ? ap.getFromDuration().toString(): null)
									.value("delete_services", ap.isDeleteServices())
									.value("organization_whitelist", String.join(",", ap.getOrganizationWhiteList()))
									.fetch()
									.rowsUpdated())
				.thenMany(Flux.fromIterable(serviceInstancePolicies)
							.flatMap(sip -> client.insert().into("service_instance_policy")
								.value("id", sip.getId())
								.value("description", sip.getDescription())
								.value("from_datetime", sip.getFromDateTime() != null ? Timestamp.valueOf(sip.getFromDateTime()): null)
								.value("from_duration", sip.getFromDuration() != null ? sip.getFromDuration().toString(): null)
								.value("organization_whitelist", String.join(",", sip.getOrganizationWhiteList()))
								.fetch()
								.rowsUpdated()))
				.then(Mono.just(new Policies(applicationPolicies, serviceInstancePolicies)));
	}

	public Mono<Policies> findServiceInstancePolicyById(String id) {
		String selectServiceInstancePolicy = "select id, description, from_datetime, from_duration, organization_whitelist from service_instance_policy where id = ?";
		List<ServiceInstancePolicy> serviceInstancePolicies = new ArrayList<>();
		return
			Flux
				.from(client.execute().sql(selectServiceInstancePolicy)
						.bind("id", id)
						.map((row, metadata) ->
							ServiceInstancePolicy
								.builder()
									.id(row.get("id", String.class))
									.description(row.get("description", String.class))
									.fromDateTime(row.get("from_datetime", Timestamp.class) != null ? row.get("from_datetime", Timestamp.class).toLocalDateTime() : null)
									.fromDuration(row.get("from_duration", String.class) != null ? Duration.parse(row.get("from_duration", String.class)): null)
									.organizationWhiteList(row.get("organization_whitelist", String.class) != null ? new HashSet<String>(Arrays.asList(row.get("organization_whitelist", String.class).split("\\s*,\\s*"))): new HashSet<>())
									.build())
						.all())
				.map(sp -> serviceInstancePolicies.add(sp))
				.then(Mono.just(new Policies(Collections.emptyList(), serviceInstancePolicies)))
				.flatMap(p -> p.isEmpty() ? Mono.empty(): Mono.just(p));
	}

	public Mono<Policies> findApplicationPolicyById(String id) {
		String selectApplicationPolicy = "select id, description, state, from_datetime, from_duration, delete_services, organization_whitelist from application_policy where id = ?";
		List<ApplicationPolicy> applicationPolicies = new ArrayList<>();
		return
			Flux
				.from(client.execute().sql(selectApplicationPolicy)
						.bind("id", id)
						.map((row, metadata) ->
							ApplicationPolicy
								.builder()
									.id(row.get("id", String.class))
									.description(row.get("description", String.class))
									.fromDateTime(row.get("from_datetime", Timestamp.class) != null ? row.get("from_datetime", Timestamp.class).toLocalDateTime() : null)
									.fromDuration(row.get("from_duration", String.class) != null ? Duration.parse(row.get("from_duration", String.class)): null)
									.organizationWhiteList(row.get("organization_whitelist", String.class) != null ? new HashSet<String>(Arrays.asList(row.get("organization_whitelist", String.class).split("\\s*,\\s*"))): new HashSet<>())
									.state(row.get("state", String.class))
									.deleteServices(row.get("delete_services", Boolean.class))
									.build())
						.all())
				.map(ap -> applicationPolicies.add(ap))
				.then(Mono.just(new Policies(applicationPolicies, Collections.emptyList())))
				.flatMap(p -> p.isEmpty() ? Mono.empty(): Mono.just(p));
	}

	public Mono<Policies> findAll() {
		String selectAllApplicationPolicies = "select id, description, state, from_datetime, from_duration, delete_services, organization_whitelist from application_policy";
		String selectAllServiceInstancePolicies = "select id, description, from_datetime, from_duration, organization_whitelist from service_instance_policy";
		List<ApplicationPolicy> applicationPolicies = new ArrayList<>();
		List<ServiceInstancePolicy> serviceInstancePolicies = new ArrayList<>();

		return
				Flux
					.from(client.execute().sql(selectAllApplicationPolicies)
							.map((row, metadata) ->
								ApplicationPolicy
									.builder()
										.id(row.get("id", String.class))
										.description(row.get("description", String.class))
										.fromDateTime(row.get("from_datetime", Timestamp.class) != null ? row.get("from_datetime", Timestamp.class).toLocalDateTime() : null)
										.fromDuration(row.get("from_duration", String.class) != null ? Duration.parse(row.get("from_duration", String.class)): null)
										.organizationWhiteList(row.get("organization_whitelist", String.class) != null ? new HashSet<String>(Arrays.asList(row.get("organization_whitelist", String.class).split("\\s*,\\s*"))): new HashSet<>())
										.state(row.get("state", String.class))
										.deleteServices(row.get("delete_services", Boolean.class))
										.build())
							.all())
					.map(ap -> applicationPolicies.add(ap))
					.thenMany(
						Flux
							.from(client.execute().sql(selectAllServiceInstancePolicies)
									.map((row, metadata) ->
										ServiceInstancePolicy
											.builder()
												.id(row.get("id", String.class))
												.description(row.get("description", String.class))
												.fromDateTime(row.get("from_datetime", Timestamp.class) != null ? row.get("from_datetime", Timestamp.class).toLocalDateTime() : null)
												.fromDuration(row.get("from_duration", String.class) != null ? Duration.parse(row.get("from_duration", String.class)): null)
												.organizationWhiteList(row.get("organization_whitelist", String.class) != null ? new HashSet<String>(Arrays.asList(row.get("organization_whitelist", String.class).split("\\s*,\\s*"))): new HashSet<>())
												.build())
									.all())
							.map(sp -> serviceInstancePolicies.add(sp)))
					.then(Mono.just(new Policies(applicationPolicies, serviceInstancePolicies)))
					.flatMap(p -> p.isEmpty() ? Mono.empty(): Mono.just(p));
	}

	public Mono<Void> deleteApplicationPolicyById(String id) {
		String deleteApplicationPolicy = "delete from application_policy where id = ?";
		return
			Flux
				.from(client.execute().sql(deleteApplicationPolicy)
					.bind("id", id)
					.fetch()
					.rowsUpdated())
				.then();
	}

	public Mono<Void> deleteServicePolicyById(String id) {
		String deleteServiceInstancePolicy = "delete from service_instance_policy where id = ?";
		return
			Flux
				.from(client.execute().sql(deleteServiceInstancePolicy)
					.bind("id" , id)
					.fetch()
					.rowsUpdated())
				.then();
	}

	public Mono<Void> deleteAll() {
		String deleteAllApplicationPolicies = "delete from application_policy";
		String deleteAllServiceInstancePolicies = "delete from service_instance_policy";
		return
			Flux
				.from(client.execute().sql(deleteAllApplicationPolicies)
					.fetch()
					.rowsUpdated())
				.thenMany(
					Flux
						.from(client.execute().sql(deleteAllServiceInstancePolicies)
							.fetch()
							.rowsUpdated()))
				.then();
	}

	private ApplicationPolicy seedApplicationPolicy(ApplicationPolicy policy) {
		return settings.isVersionManaged() ? ApplicationPolicy.seedWith(policy, settings.getCommit()): ApplicationPolicy.seed(policy);
	}

	private ServiceInstancePolicy seedServiceInstancePolicy(ServiceInstancePolicy policy) {
		return settings.isVersionManaged() ? ServiceInstancePolicy.seedWith(policy, settings.getCommit()): ServiceInstancePolicy.seed(policy);
	}
}
