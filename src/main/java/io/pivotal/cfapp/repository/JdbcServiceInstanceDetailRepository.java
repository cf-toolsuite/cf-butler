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

@Profile("jdbc")
@Repository
public class JdbcServiceInstanceDetailRepository {

	private Database database;

	@Autowired
	public JdbcServiceInstanceDetailRepository(Database database) {
		this.database = database;
	}

	public Mono<ServiceInstanceDetail> save(ServiceInstanceDetail entity) {
		String createOne = "insert into service_detail (organization, space, service_id, name, service, description, plan, type, bound_applications, last_operation, last_updated, dashboard_url, requested_state) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		Flowable<Integer> insert = database
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
			.returnGeneratedKeys()
			.getAs(Integer.class);

		String selectOne = "select id, organization, space, service_id, name, service, description, plan, type, bound_applications, last_operation, last_updated, dashboard_url, requested_state from service_detail where id = ?";
		Flowable<ServiceInstanceDetail> result = database
			.select(selectOne)
			.parameterStream(insert)
			.get(rs -> fromResultSet(rs));
		return Mono.from(result);
	}

	public Flux<ServiceInstanceDetail> findAll() {
		String selectAll = "select id, organization, space, service_id, name, service, description, plan, type, bound_applications, last_operation, last_updated, dashboard_url, requested_state from service_detail order by organization, space, service, name";
		Flowable<ServiceInstanceDetail> result = database
			.select(selectAll)
			.get(rs -> fromResultSet(rs));
		return Flux.from(result);
	}

	public Mono<Void> deleteAll() {
		String deleteAll = "delete from service_detail";
		Flowable<Integer> result = database
			.update(deleteAll)
			.counts();
		return Flux.from(result).then();
	}
	
	public Flux<ServiceInstanceDetail> findByServiceInstancePolicy(ServiceInstancePolicy policy) {
		String select = "select id, organization, space, service_id, name, service, description, plan, type, bound_applications, last_operation, last_updated, dashboard_url, requested_state";
		String from = "from service_detail";
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
		Flowable<ServiceInstanceDetail> result = 
			database
				.select(sql)
				.parameters(paramValues)
				.get(rs -> fromResultSet(rs));
		return Flux.from(result);
	}
	
	private ServiceInstanceDetail fromResultSet(ResultSet rs) throws SQLException {
		return ServiceInstanceDetail
				.builder()
					.id(String.valueOf(rs.getInt(1)))
					.organization(rs.getString(2))
					.space(rs.getString(3))
					.serviceId(rs.getString(4))
					.name(rs.getString(5))
					.service(rs.getString(6))
					.description(rs.getString(7))
					.plan(rs.getString(8))
					.type(rs.getString(9))
					.applications(rs.getString(10) != null ? Arrays.asList(rs.getString(10).split("\\s*,\\s*")): Collections.emptyList())
					.lastOperation(rs.getString(11))
					.lastUpdated(rs.getTimestamp(12) != null ? rs.getTimestamp(12).toLocalDateTime(): null)
					.dashboardUrl(rs.getString(13))
					.requestedState(rs.getString(14))
					.build();
	}
}