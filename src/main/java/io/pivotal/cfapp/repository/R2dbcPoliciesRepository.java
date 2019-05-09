package io.pivotal.cfapp.repository;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.function.DatabaseClient;
import org.springframework.data.r2dbc.function.DatabaseClient.GenericInsertSpec;
import org.springframework.stereotype.Repository;

import io.pivotal.cfapp.config.PoliciesSettings;
import io.pivotal.cfapp.config.DbmsSettings;
import io.pivotal.cfapp.domain.ApplicationPolicy;
import io.pivotal.cfapp.domain.Defaults;
import io.pivotal.cfapp.domain.Policies;
import io.pivotal.cfapp.domain.ServiceInstancePolicy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class R2dbcPoliciesRepository {

	private final DatabaseClient client;
	private final PoliciesSettings policiesSettings;
	private final DbmsSettings dbmsSettings;

	@Autowired
	public R2dbcPoliciesRepository(
		DatabaseClient client,
		PoliciesSettings policiesSettings,
		DbmsSettings dbmsSettings) {
		this.client = client;
		this.policiesSettings = policiesSettings;
		this.dbmsSettings = dbmsSettings;
	}

	public Mono<Policies> save(Policies entity) {
		List<ApplicationPolicy> applicationPolicies = entity.getApplicationPolicies().stream()
				.filter(ap -> !ap.isInvalid()).map(p -> seedApplicationPolicy(p)).collect(Collectors.toList());

		List<ServiceInstancePolicy> serviceInstancePolicies = entity.getServiceInstancePolicies().stream()
				.filter(sip -> !sip.isInvalid()).map(p -> seedServiceInstancePolicy(p)).collect(Collectors.toList());

		return Flux.fromIterable(applicationPolicies)
					.flatMap(ap -> saveApplicationPolicy(ap))
					.thenMany(Flux.fromIterable(serviceInstancePolicies)
					.flatMap(sip -> saveServiceInstancePolicy(sip)))
					.then(Mono.just(new Policies(applicationPolicies, serviceInstancePolicies)));
	}

	public Mono<Policies> findServiceInstancePolicyById(String id) {
		String index = dbmsSettings.getBindPrefix() + 1;
		String selectServiceInstancePolicy = "select pk, id, description, from_datetime, from_duration, organization_whitelist from service_instance_policy where id = " + index;
		List<ServiceInstancePolicy> serviceInstancePolicies = new ArrayList<>();
		return
			Flux
				.from(client.execute().sql(selectServiceInstancePolicy)
						.bind(index, id)
						.map((row, metadata) ->
							ServiceInstancePolicy
								.builder()
									.pk(row.get("pk", Long.class))
									.id(row.get("id", String.class))
									.description(Defaults.getValueOrDefault(row.get("description", String.class), ""))
									.fromDateTime(Defaults.getValueOrDefault(row.get("from_datetime", Timestamp.class), Timestamp.valueOf(LocalDateTime.MIN)).toLocalDateTime())
									.fromDuration(row.get("from_duration", String.class) != null ? Duration.parse(row.get("from_duration", String.class)): null)
									.organizationWhiteList(row.get("organization_whitelist", String.class) != null ? new HashSet<String>(Arrays.asList(row.get("organization_whitelist", String.class).split("\\s*,\\s*"))): new HashSet<>())
									.build())
						.all())
				.map(sp -> serviceInstancePolicies.add(sp))
				.then(Mono.just(new Policies(Collections.emptyList(), serviceInstancePolicies)))
				.flatMap(p -> p.isEmpty() ? Mono.empty(): Mono.just(p));
	}

	public Mono<Policies> findApplicationPolicyById(String id) {
		String index = dbmsSettings.getBindPrefix() + 1;
		String selectApplicationPolicy = "select pk, id, description, state, from_datetime, from_duration, delete_services, organization_whitelist from application_policy where id = " + index;
		List<ApplicationPolicy> applicationPolicies = new ArrayList<>();
		return
			Flux
				.from(client.execute().sql(selectApplicationPolicy)
						.bind(index, id)
						.map((row, metadata) ->
							ApplicationPolicy
								.builder()
									.pk(row.get("pk", Long.class))
									.id(row.get("id", String.class))
									.description(Defaults.getValueOrDefault(row.get("description", String.class), ""))
									.fromDateTime(Defaults.getValueOrDefault(row.get("from_datetime", Timestamp.class), Timestamp.valueOf(LocalDateTime.MIN)).toLocalDateTime())
									.fromDuration(row.get("from_duration", String.class) != null ? Duration.parse(row.get("from_duration", String.class)): null)
									.organizationWhiteList(row.get("organization_whitelist", String.class) != null ? new HashSet<String>(Arrays.asList(row.get("organization_whitelist", String.class).split("\\s*,\\s*"))): new HashSet<>())
									.state(Defaults.getValueOrDefault(row.get("state", String.class), ""))
									.deleteServices(row.get("delete_services", Boolean.class))
									.build())
						.all())
				.map(ap -> applicationPolicies.add(ap))
				.then(Mono.just(new Policies(applicationPolicies, Collections.emptyList())))
				.flatMap(p -> p.isEmpty() ? Mono.empty(): Mono.just(p));
	}

	public Mono<Policies> findAll() {
		String selectAllApplicationPolicies = "select pk, id, description, state, from_datetime, from_duration, delete_services, organization_whitelist from application_policy";
		String selectAllServiceInstancePolicies = "select pk, id, description, from_datetime, from_duration, organization_whitelist from service_instance_policy";
		List<ApplicationPolicy> applicationPolicies = new ArrayList<>();
		List<ServiceInstancePolicy> serviceInstancePolicies = new ArrayList<>();

		return
				Flux
					.from(client.execute().sql(selectAllApplicationPolicies)
							.map((row, metadata) ->
								ApplicationPolicy
									.builder()
										.pk(row.get("pk", Long.class))
										.id(row.get("id", String.class))
										.description(row.get("description", String.class))
										.fromDateTime(Defaults.getValueOrDefault(row.get("from_datetime", Timestamp.class), Timestamp.valueOf(LocalDateTime.MIN)).toLocalDateTime())
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
												.pk(row.get("pk", Long.class))
												.id(row.get("id", String.class))
												.description(Defaults.getValueOrDefault(row.get("description", String.class), ""))
												.fromDateTime(Defaults.getValueOrDefault(row.get("from_datetime", Timestamp.class), Timestamp.valueOf(LocalDateTime.MIN)).toLocalDateTime())
												.fromDuration(row.get("from_duration", String.class) != null ? Duration.parse(row.get("from_duration", String.class)): null)
												.organizationWhiteList(row.get("organization_whitelist", String.class) != null ? new HashSet<String>(Arrays.asList(row.get("organization_whitelist", String.class).split("\\s*,\\s*"))): new HashSet<>())
												.build())
									.all())
							.map(sp -> serviceInstancePolicies.add(sp)))
					.then(Mono.just(new Policies(applicationPolicies, serviceInstancePolicies)))
					.flatMap(p -> p.isEmpty() ? Mono.empty(): Mono.just(p));
	}

	public Mono<Void> deleteApplicationPolicyById(String id) {
		String index = dbmsSettings.getBindPrefix() + 1;
		String deleteApplicationPolicy = "delete from application_policy where id = " + index;
		return
			Flux
				.from(client.execute().sql(deleteApplicationPolicy)
					.bind(index, id)
					.fetch()
					.rowsUpdated())
				.then();
	}

	public Mono<Void> deleteServicePolicyById(String id) {
		String index = dbmsSettings.getBindPrefix() + 1;
		String deleteServiceInstancePolicy = "delete from service_instance_policy where id = " + index;
		return
			Flux
				.from(client.execute().sql(deleteServiceInstancePolicy)
					.bind(index, id)
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
		return policiesSettings.isVersionManaged() ? ApplicationPolicy.seedWith(policy, policiesSettings.getCommit()): ApplicationPolicy.seed(policy);
	}

	private ServiceInstancePolicy seedServiceInstancePolicy(ServiceInstancePolicy policy) {
		return policiesSettings.isVersionManaged() ? ServiceInstancePolicy.seedWith(policy, policiesSettings.getCommit()): ServiceInstancePolicy.seed(policy);
	}

	private Mono<Integer> saveApplicationPolicy(ApplicationPolicy ap) {
		GenericInsertSpec<Map<String, Object>> spec =
			client.insert().into("application_policy")
				.value("id", ap.getId());
		if (ap.getDescription() != null) {
			spec = spec.value("description", ap.getDescription());
		} else {
			spec = spec.nullValue("description");
		}
		if (ap.getState() != null) {
			spec = spec.value("state", ap.getState());
		} else {
			spec = spec.nullValue("state");
		}
		if (ap.getFromDateTime() != null) {
			spec = spec.value("from_datetime", Timestamp.valueOf(ap.getFromDateTime()));
		} else {
			spec = spec.nullValue("from_datetime");
		}
		if (ap.getFromDuration() != null) {
			spec = spec.value("from_duration", ap.getFromDuration().toString());
		} else {
			spec = spec.nullValue("from_duration");
		}
		spec = spec.value("delete_services", ap.isDeleteServices());
		spec = spec.value("organization_whitelist", String.join(",", ap.getOrganizationWhiteList()));
		return spec.fetch().rowsUpdated();
	}

	private Mono<Integer> saveServiceInstancePolicy(ServiceInstancePolicy sip) {
		GenericInsertSpec<Map<String, Object>> spec =
			client.insert().into("service_instance_policy")
				.value("id", sip.getId());
		if (sip.getDescription() != null) {
			spec = spec.value("description", sip.getDescription());
		} else {
			spec = spec.nullValue("description");
		}
		if (sip.getFromDateTime() != null) {
			spec = spec.value("from_datetime", Timestamp.valueOf(sip.getFromDateTime()));
		} else {
			spec = spec.nullValue("from_datetime");
		}
		if (sip.getFromDuration() != null) {
			spec = spec.value("from_duration", sip.getFromDuration().toString());
		} else {
			spec = spec.nullValue("from_duration");
		}
		spec = spec.value("organization_whitelist", String.join(",", sip.getOrganizationWhiteList()));
		return spec.fetch().rowsUpdated();
	}
}
