package io.pivotal.cfapp.repository;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
		String createAppPolicy = "insert into application_policy (description, state, from_datetime, from_duration, delete_services, organization_whitelist) values (?, ?, ?, ?, ?, ?)";
		String createServiceInstancePolicy = "insert into service_instance_policy (description, from_datetime, from_duration, organization_whitelist) values (?, ?, ?, ?)";
		List<ApplicationPolicy> applicationPolicies = entity.getApplicationPolicies()
				.stream()
				.filter(ap -> !ap.isInvalid())
				.collect(Collectors.toList());
		
		List<ServiceInstancePolicy> serviceInstancePolicies = entity.getServiceInstancePolicies()
				.stream()
				.filter(sip -> !sip.isInvalid())
				.collect(Collectors.toList());
		
		return Flux.fromIterable(applicationPolicies)
				.flatMap(ap -> database
									.update(createAppPolicy)
									.parameters(
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
										sip.getDescription(),
										sip.getFromDateTime() != null ? Timestamp.valueOf(sip.getFromDateTime()): null,
										sip.getFromDuration() != null ? sip.getFromDuration().toString(): null,
										String.join(",", sip.getOrganizationWhiteList())
										)
								.counts()))
				.then(Mono.just(new Policies(applicationPolicies, serviceInstancePolicies)));
	}

	public Mono<Policies> findAll() {
		String selectAllApplicationPolicies = "select description, state, from_datetime, from_duration, delete_services from application_policy";
		String selectAllServiceInstancePolicies = "select description, from_datetime, from_duration from service_instance_policy";
		List<ApplicationPolicy> applicationPolicies = new ArrayList<>();
		List<ServiceInstancePolicy> serviceInstancePolicies = new ArrayList<>();
		
		return 
				Flux
					.from(database
							.select(selectAllApplicationPolicies)
							.get(rs -> new ApplicationPolicy(
									rs.getString(1),
									rs.getString(2),
									rs.getTimestamp(3) != null ? rs.getTimestamp(3).toLocalDateTime(): null,
									rs.getString(4) != null ? Duration.parse(rs.getString(4)): null,
									rs.getBoolean(5),
									rs.getString(6) != null ? Arrays.asList(rs.getString(6).split("\\s*,\\s*")): Collections.emptyList()
							))
					.map(ap -> applicationPolicies.add(ap)))
					.thenMany(
						Flux
							.from(database
									.select(selectAllServiceInstancePolicies)
									.get(rs -> new ServiceInstancePolicy(
											rs.getString(1),
											rs.getTimestamp(2) != null ? rs.getTimestamp(2).toLocalDateTime(): null,
											rs.getString(3) != null ? Duration.parse(rs.getString(3)): null,
											rs.getString(4) != null ? Arrays.asList(rs.getString(4).split("\\s*,\\s*")): Collections.emptyList()		
									))
							.map(sp -> serviceInstancePolicies.add(sp))))
					.then(Mono.just(new Policies(applicationPolicies, serviceInstancePolicies)));
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
