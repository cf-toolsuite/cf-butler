package io.pivotal.cfapp.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.davidmoten.rx.jdbc.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import io.pivotal.cfapp.domain.ServiceInstanceDetail;
import io.pivotal.cfapp.domain.ServiceInstancePolicy;
import io.reactivex.Flowable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Profile("jdbc")
@Repository
public class JdbcServiceInstanceDetailRepository {

	private Database database;

	@Autowired
	public JdbcServiceInstanceDetailRepository(Database database) {
		this.database = database;
	}

	public Mono<ServiceInstanceDetail> save(ServiceInstanceDetail entity) {
		String createOne = "insert into service_instance_detail (organization, space, service_id, service_name, service, description, plan, type, bound_applications, last_operation, last_updated, dashboard_url, requested_state) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		return Mono
				.from(
					database
						.update(createOne)
						.parameters(
							entity.getOrganization(),
							entity.getSpace(),
							entity.getServiceId(),
							entity.getName(),
							entity.getService(),
							entity.getDescription(),
							entity.getPlan(),
							entity.getType(),
							String.join(",", entity.getApplications()),
							entity.getLastOperation(),
							entity.getLastUpdated() != null ? Timestamp.valueOf(entity.getLastUpdated()): null,
							entity.getDashboardUrl(),
							entity.getRequestedState()
						)
						.counts())
						.then(Mono.just(entity));
	}

	public Flux<ServiceInstanceDetail> findAll() {
		String selectAll = "select organization, space, service_id, name, service, description, plan, type, bound_applications, last_operation, last_updated, dashboard_url, requested_state from service_instance_detail order by organization, space, service, name";
		Flowable<ServiceInstanceDetail> result = database
			.select(selectAll)
			.get(rs -> fromResultSet(rs));
		return Flux.from(result);
	}

	public Mono<Void> deleteAll() {
		String deleteAll = "delete from service_instance_detail";
		Flowable<Integer> result = database
			.update(deleteAll)
			.counts();
		return Flux.from(result).then();
	}
	
	public Flux<Tuple2<ServiceInstanceDetail, ServiceInstancePolicy>> findByServiceInstancePolicy(ServiceInstancePolicy policy) {
		String select = "select organization, space, service_id, service_name, service, description, plan, type, bound_applications, last_operation, last_updated, dashboard_url, requested_state";
		String from = "from service_instance_detail";
		StringBuilder where = new StringBuilder();
		List<Object> paramValues = new ArrayList<>();
		where.append("where bound_applications is null "); // orphans only
		if (policy.getFromDateTime() != null) {
			where.append("and last_updated <= ? ");
			paramValues.add(Timestamp.valueOf(policy.getFromDateTime()));
		}
		if (policy.getFromDuration() != null) {
			where.append("and last_updated <= ?");
			LocalDateTime eventTime = LocalDateTime.now().minus(policy.getFromDuration());
			paramValues.add(Timestamp.valueOf(eventTime));
		}
		String orderBy = "order by organization, space, name";
		String sql = String.join(" ", select, from, where, orderBy);
		Flowable<Tuple2<ServiceInstanceDetail, ServiceInstancePolicy>> result = 
			database
				.select(sql)
				.parameters(paramValues)
				.get(rs -> toTuple(fromResultSet(rs), policy));
		return Flux.from(result);
	}
	
	private Tuple2<ServiceInstanceDetail, ServiceInstancePolicy> toTuple(ServiceInstanceDetail detail, ServiceInstancePolicy policy) {
		return Tuples.of(detail, policy);
	}

	private ServiceInstanceDetail fromResultSet(ResultSet rs) throws SQLException {
		return ServiceInstanceDetail
				.builder()
					.organization(rs.getString(1))
					.space(rs.getString(2))
					.serviceId(rs.getString(3))
					.name(rs.getString(4))
					.service(rs.getString(5))
					.description(rs.getString(6))
					.plan(rs.getString(7))
					.type(rs.getString(8))
					.applications(rs.getString(9) != null ? Arrays.asList(rs.getString(9).split("\\s*,\\s*")): Collections.emptyList())
					.lastOperation(rs.getString(10))
					.lastUpdated(rs.getTimestamp(11) != null ? rs.getTimestamp(11).toLocalDateTime(): null)
					.dashboardUrl(rs.getString(12))
					.requestedState(rs.getString(13))
					.build();
	}
}