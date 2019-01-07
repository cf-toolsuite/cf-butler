package io.pivotal.cfapp.repository;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.davidmoten.rx.jdbc.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import io.pivotal.cfapp.domain.ApplicationPolicy;
import io.pivotal.cfapp.domain.Policies;
import io.pivotal.cfapp.domain.ServiceInstancePolicy;
import io.reactivex.Flowable;
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
		String createAppPolicy = "insert into application_policy (description, state, from_datetime, from_duration, unbind_services, delete_services) values (?, ?, ?, ?, ?, ?)";
		String createServiceInstancePolicy = "insert into service_instance_policy (description, from_datetime, from_duration) values (?, ?, ?)";
		List<ApplicationPolicy> applicationPolicies = entity.getApplicationPolicies()
				.stream()
				.filter(ap -> !ap.isInvalid())
				.collect(Collectors.toList());
		
		Flux.fromIterable(applicationPolicies)
				.flatMap(ap -> database
									.update(createAppPolicy)
									.parameters(
										ap.getDescription(),
										ap.getState(),
										ap.getFromDateTime() != null ? Timestamp.valueOf(ap.getFromDateTime()): null,
										ap.getFromDuration() != null ? ap.getFromDuration().toString(): null,
										ap.isUnbindServices(),
										ap.isDeleteServices()
									)
									.counts())
				.subscribe();
				
		List<ServiceInstancePolicy> serviceInstancePolicies = entity.getServiceInstancePolicies()
				.stream()
				.filter(sip -> !sip.isInvalid())
				.collect(Collectors.toList());
		
		Flux.fromIterable(serviceInstancePolicies)
				.flatMap(sip -> database
									.update(createServiceInstancePolicy)
									.parameters(
										sip.getDescription(),
										sip.getFromDateTime() != null ? Timestamp.valueOf(sip.getFromDateTime()): null,
										sip.getFromDuration() != null ? sip.getFromDuration().toString(): null
									)
									.counts())
				.subscribe();
		
		return Mono.just(new Policies(applicationPolicies, serviceInstancePolicies));
	}

	public Mono<Policies> findAll() {
		String selectAllApplicationPolicies = "select description, state, from_datetime, from_duration, unbind_services, delete_services from application_policy";
		String selectAllServiceInstancePolicies = "select description, from_datetime, from_duration from service_instance_policy";
		
		Flowable<ApplicationPolicy> selectAllApplicationPoliciesResult = database
				.select(selectAllApplicationPolicies)
				.get(rs -> new ApplicationPolicy(
						rs.getString(1),
						rs.getString(2),
						rs.getTimestamp(3) != null ? rs.getTimestamp(3).toLocalDateTime(): null,
						rs.getString(4) != null ? Duration.parse(rs.getString(4)): null,
						rs.getBoolean(5),
						rs.getBoolean(6)
				));
		
		Flowable<ServiceInstancePolicy> selectAllServiceInstancePoliciesResult = database
				.select(selectAllServiceInstancePolicies)
				.get(rs -> new ServiceInstancePolicy(
						rs.getString(1),
						rs.getTimestamp(2) != null ? rs.getTimestamp(2).toLocalDateTime(): null,
						rs.getString(3) != null ? Duration.parse(rs.getString(3)): null
				));
		
		List<ApplicationPolicy> applicationPolicies = new ArrayList<>();
				Flux.from(selectAllApplicationPoliciesResult)
							.map(r -> applicationPolicies.add(r))
							.subscribe();
				
		List<ServiceInstancePolicy> serviceInstancePolicies = new ArrayList<>();
				Flux.from(selectAllServiceInstancePoliciesResult)
							.map(r -> serviceInstancePolicies.add(r))
							.subscribe();
		
		return Mono.just(new Policies(applicationPolicies, serviceInstancePolicies));
	}

	public Mono<Void> deleteAll() {
		String deleteAllApplicationPolicies = "delete from application_policy";
		String deleteAllServiceInstancePolicies = "delete from service_instance_policy";
		Flux.from(database
					.update(deleteAllApplicationPolicies)
					.counts())
			.subscribe();
		Flux.from(database
					.update(deleteAllServiceInstancePolicies)
					.counts())
			.subscribe();
		return Mono.empty();
	}
}
