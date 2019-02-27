package io.pivotal.cfapp.repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.function.DatabaseClient;
import org.springframework.stereotype.Repository;

import io.pivotal.cfapp.domain.ServiceInstanceDetail;
import io.pivotal.cfapp.domain.ServiceInstancePolicy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Repository
public class R2dbcServiceInstanceDetailRepository {

	private DatabaseClient client;

	@Autowired
	public R2dbcServiceInstanceDetailRepository(DatabaseClient client) {
		this.client = client;
	}

	public Mono<ServiceInstanceDetail> save(ServiceInstanceDetail entity) {
		return client.insert().into("service_instance_detail")
						.value("organization", entity.getOrganization())
						.value("space", entity.getSpace())
						.value("service_id", entity.getServiceId())
						.value("service_name", entity.getName())
						.value("description", entity.getDescription())
						.value("plan", entity.getPlan())
						.value("type", entity.getType())
						.value("bound_applications", String.join(",", entity.getApplications()))
						.value("last_operation", entity.getLastOperation())
						.value("last_updated", entity.getLastUpdated() != null ? Timestamp.valueOf(entity.getLastUpdated()): null)
						.value("dashboard_url", entity.getDashboardUrl())
						.value("requested_state", entity.getRequestedState())
						.fetch()
						.rowsUpdated()
						.then(Mono.just(entity));
	}

	public Flux<ServiceInstanceDetail> findAll() {
		return client.select().from("application_detail")
						.orderBy(Sort.by("organization", "space", "service", "name"))
						.as(ServiceInstanceDetail.class)
						.fetch()
						.all();
	}

	public Mono<Void> deleteAll() {
		return client.execute().sql("delete from service_instance_detail")
						.fetch()
						.rowsUpdated()
						.then();
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
		return client.execute().sql(sql).as(ServiceInstanceDetail.class)
						.fetch().all().map(r -> toTuple(r, policy));
	}

	private Tuple2<ServiceInstanceDetail, ServiceInstancePolicy> toTuple(ServiceInstanceDetail detail, ServiceInstancePolicy policy) {
		return Tuples.of(detail, policy);
	}

}