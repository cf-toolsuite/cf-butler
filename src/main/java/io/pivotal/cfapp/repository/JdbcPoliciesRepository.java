package io.pivotal.cfapp.repository;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.davidmoten.rx.jdbc.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import io.pivotal.cfapp.domain.ApplicationPolicy;
import io.pivotal.cfapp.domain.Policies;
import io.pivotal.cfapp.domain.ServiceInstancePolicy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("jdbc")
@Repository
public class JdbcPoliciesRepository {

	private Database database;

	@Autowired
	public JdbcPoliciesRepository(Database database) {
		this.database = database;
	}
	
	public Mono<Policies> save(Policies entity) {
		String createAppPolicy = "insert into application_policy (id, description, state, from_datetime, from_duration, delete_services, organization_whitelist) values (?, ?, ?, ?, ?, ?, ?)";
		String createServiceInstancePolicy = "insert into service_instance_policy (id, description, from_datetime, from_duration, organization_whitelist) values (?, ?, ?, ?, ?)";
		List<ApplicationPolicy> applicationPolicies = entity.getApplicationPolicies()
				.stream()
				.filter(ap -> !ap.isInvalid())
				.map(p -> ApplicationPolicy.seed(p))
				.collect(Collectors.toList());
		
		List<ServiceInstancePolicy> serviceInstancePolicies = entity.getServiceInstancePolicies()
				.stream()
				.filter(sip -> !sip.isInvalid())
				.map(p -> ServiceInstancePolicy.seed(p))
				.collect(Collectors.toList());
		
		return Flux.fromIterable(applicationPolicies)
				.flatMap(ap -> database
									.update(createAppPolicy)
									.parameters(
										ap.getId(),
										ap.getDescription(),
										ap.getState(),
										ap.getFromDateTime() != null ? Timestamp.valueOf(ap.getFromDateTime()): null,
										ap.getFromDuration() != null ? ap.getFromDuration().toString(): null,
										ap.isDeleteServices(),
										String.join(",", ap.getOrganizationWhiteList())
									)
									.counts())
				.thenMany(
						Flux.fromIterable(serviceInstancePolicies)
							.flatMap(sip -> database
								.update(createServiceInstancePolicy)
								.parameters(
										sip.getId(),
										sip.getDescription(),
										sip.getFromDateTime() != null ? Timestamp.valueOf(sip.getFromDateTime()): null,
										sip.getFromDuration() != null ? sip.getFromDuration().toString(): null,
										String.join(",", sip.getOrganizationWhiteList())
										)
								.counts()))
				.then(Mono.just(new Policies(applicationPolicies, serviceInstancePolicies)));
	}

	public Mono<Policies> findServiceInstancePolicyById(String id) {
		String selectServiceInstancePolicy = "select id, description, from_datetime, from_duration, organization_whitelist from service_instance_policy where id = ?";
		List<ServiceInstancePolicy> serviceInstancePolicies = new ArrayList<>();
		return 
			Flux
				.from(database
						.select(selectServiceInstancePolicy)
						.parameter(id)
						.get(rs -> ServiceInstancePolicy
									.builder()
										.id(rs.getString(1))
										.description(rs.getString(2))
										.fromDateTime(rs.getTimestamp(3) != null ? rs.getTimestamp(3).toLocalDateTime(): null)
										.fromDuration(rs.getString(4) != null ? Duration.parse(rs.getString(4)): null)
										.organizationWhiteList(rs.getString(5) != null ? new HashSet<String>(Arrays.asList(rs.getString(5).split("\\s*,\\s*"))): new HashSet<>())
										.build()
						))
				.map(sp -> serviceInstancePolicies.add(sp))
				.then(Mono.just(new Policies(Collections.emptyList(), serviceInstancePolicies)))
				.flatMap(p -> p.isEmpty() ? Mono.empty(): Mono.just(p));
	}
	
	public Mono<Policies> findApplicationPolicyById(String id) {
		String selectApplicationPolicy = "select id, description, state, from_datetime, from_duration, delete_services, organization_whitelist from application_policy where id = ?";
		List<ApplicationPolicy> applicationPolicies = new ArrayList<>();
		return 
			Flux
				.from(database
						.select(selectApplicationPolicy)
						.parameter(id)
						.get(rs -> ApplicationPolicy
									.builder()
										.id(rs.getString(1))
										.description(rs.getString(2))
										.state(rs.getString(3))
										.fromDateTime(rs.getTimestamp(4) != null ? rs.getTimestamp(4).toLocalDateTime(): null)
										.fromDuration(rs.getString(5) != null ? Duration.parse(rs.getString(5)): null)
										.deleteServices(rs.getBoolean(6))
										.organizationWhiteList(rs.getString(7) != null ? new HashSet<String>(Arrays.asList(rs.getString(7).split("\\s*,\\s*"))): new HashSet<>())
										.build()
						))
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
					.from(database
							.select(selectAllApplicationPolicies)
							.get(rs -> ApplicationPolicy
										.builder()
											.id(rs.getString(1))
											.description(rs.getString(2))
											.state(rs.getString(3))
											.fromDateTime(rs.getTimestamp(4) != null ? rs.getTimestamp(4).toLocalDateTime(): null)
											.fromDuration(rs.getString(5) != null ? Duration.parse(rs.getString(5)): null)
											.deleteServices(rs.getBoolean(6))
											.organizationWhiteList(rs.getString(7) != null ? new HashSet<String>(Arrays.asList(rs.getString(7).split("\\s*,\\s*"))): new HashSet<>())
											.build()
							))
					.map(ap -> applicationPolicies.add(ap))
					.thenMany(
						Flux
							.from(database
									.select(selectAllServiceInstancePolicies)
									.get(rs -> ServiceInstancePolicy
												.builder()
													.id(rs.getString(1))
													.description(rs.getString(2))
													.fromDateTime(rs.getTimestamp(3) != null ? rs.getTimestamp(3).toLocalDateTime(): null)
													.fromDuration(rs.getString(4) != null ? Duration.parse(rs.getString(4)): null)
													.organizationWhiteList(rs.getString(5) != null ? new HashSet<String>(Arrays.asList(rs.getString(5).split("\\s*,\\s*"))): new HashSet<>())
													.build()
									))
							.map(sp -> serviceInstancePolicies.add(sp)))
					.then(Mono.just(new Policies(applicationPolicies, serviceInstancePolicies)))
					.flatMap(p -> p.isEmpty() ? Mono.empty(): Mono.just(p));
	}

	public Mono<Void> deleteApplicationPolicyById(String id) {
		String deleteApplicationPolicy = "delete from application_policy where id = ?";
		return 
			Flux
				.from(database
					.update(deleteApplicationPolicy)
					.parameter(id)
					.counts())
				.then();
	}
	
	public Mono<Void> deleteServicePolicyById(String id) {
		String deleteServiceInstancePolicy = "delete from service_instance_policy where id = ?";
		return 
			Flux
				.from(database
					.update(deleteServiceInstancePolicy)
					.parameter(id)
					.counts())
				.then();
	}
	
	public Mono<Void> deleteAll() {
		String deleteAllApplicationPolicies = "delete from application_policy";
		String deleteAllServiceInstancePolicies = "delete from service_instance_policy";
		return 
			Flux
				.from(database
					.update(deleteAllApplicationPolicies)
					.counts())
				.thenMany(
					Flux
						.from(database
							.update(deleteAllServiceInstancePolicies)
							.counts()))
				.then();
	}
}
