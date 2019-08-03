package io.pivotal.cfapp.repository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.data.r2dbc.core.DatabaseClient.GenericInsertSpec;
import org.springframework.data.r2dbc.query.Criteria;
import org.springframework.stereotype.Repository;

import io.pivotal.cfapp.domain.Defaults;
import io.pivotal.cfapp.domain.ServiceInstanceDetail;
import io.pivotal.cfapp.domain.ServiceInstancePolicy;
import io.r2dbc.spi.Row;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Repository
public class R2dbcServiceInstanceDetailRepository {

	private final DatabaseClient client;

	@Autowired
	public R2dbcServiceInstanceDetailRepository(DatabaseClient client) {
		this.client = client;
	}

	public Mono<ServiceInstanceDetail> save(ServiceInstanceDetail entity) {
		GenericInsertSpec<Map<String, Object>> spec = client.insert().into(ServiceInstanceDetail.tableName())
				.value("organization", entity.getOrganization());
		spec = spec.value("space", entity.getSpace());
		if (entity.getServiceInstanceId() != null) {
			spec = spec.value("service_instance_id", entity.getServiceInstanceId());
		} else {
			spec = spec.nullValue("service_instance_id");
		}
		if (entity.getName() != null) {
			spec = spec.value("service_name", entity.getName());
		} else {
			spec = spec.nullValue("service_name");
		}
		if (entity.getService() != null) {
			spec = spec.value("service", entity.getService());
		} else {
			spec = spec.nullValue("service");
		}
		if (entity.getDescription() != null) {
			spec = spec.value("description", entity.getDescription());
		} else {
			spec = spec.nullValue("description");
		}
		if (entity.getPlan() != null) {
			spec = spec.value("plan", entity.getPlan());
		} else {
			spec = spec.nullValue("plan");
		}
		if (entity.getType() != null) {
			spec = spec.value("type", entity.getType());
		} else {
			spec = spec.nullValue("type");
		}
		if (entity.getApplications() != null) {
			spec = spec.value("bound_applications", String.join(",", entity.getApplications()));
		} else {
			spec = spec.nullValue("bound_applications");
		}
		if (entity.getLastOperation() != null) {
			spec = spec.value("last_operation", entity.getLastOperation());
		} else {
			spec = spec.nullValue("last_operation");
		}
		if (entity.getLastUpdated() != null) {
			spec = spec.value("last_updated", entity.getLastUpdated());
		} else {
			spec = spec.nullValue("last_updated");
		}
		if (entity.getDashboardUrl() != null) {
			spec = spec.value("dashboard_url", entity.getDashboardUrl());
		} else {
			spec = spec.nullValue("dashboard_url");
		}
		if (entity.getRequestedState() != null) {
			spec = spec.value("requested_state", entity.getRequestedState());
		} else {
			spec = spec.nullValue("requested_state");
		}
		return spec.fetch().rowsUpdated().then(Mono.just(entity));
	}

	public Flux<ServiceInstanceDetail> findAll() {
		return client
				.select()
				.from(ServiceInstanceDetail.tableName())
				.project(ServiceInstanceDetail.columnNames())
				.orderBy(Order.asc("organization"), Order.asc("space"), Order.asc("service"), Order.asc("service_name"))
				.map((row, metadata) -> fromRow(row))
				.all();
	}

	private ServiceInstanceDetail fromRow(Row row) {
		return ServiceInstanceDetail
				.builder()
					.pk(row.get("pk", Long.class))
					.organization(Defaults.getColumnValueOrDefault(row, "organization", String.class, ""))
					.space(Defaults.getColumnValueOrDefault(row, "space", String.class, ""))
					.serviceInstanceId(Defaults.getColumnValueOrDefault(row, "service_instance_id", String.class, ""))
					.name(Defaults.getColumnValueOrDefault(row, "service_name", String.class, ""))
					.service(Defaults.getColumnValueOrDefault(row, "service", String.class, ""))
					.description(Defaults.getColumnValueOrDefault(row, "description", String.class, ""))
					.type(Defaults.getColumnValueOrDefault(row, "type", String.class, ""))
					.plan(Defaults.getColumnValueOrDefault(row, "plan", String.class, ""))
					.applications(
						Arrays.asList(Defaults.getColumnValueOrDefault(row, "bound_applications", String.class, "").split("\\s*,\\s*")))
					.lastOperation(Defaults.getColumnValueOrDefault(row, "last_operation", String.class, ""))
					.dashboardUrl(Defaults.getColumnValueOrDefault(row, "dashboard_url", String.class, ""))
					.lastUpdated(Defaults.getColumnValueOrDefault(row, "last_updated", LocalDateTime.class, null))
					.requestedState(Defaults.getColumnValueOrDefault(row, "requested_state", String.class, ""))
					.build();
	}

	public Mono<Void> deleteAll() {
		return client
				.delete()
				.from(ServiceInstanceDetail.tableName())
				.fetch()
				.rowsUpdated()
				.then();
	}

	public Flux<Tuple2<ServiceInstanceDetail, ServiceInstancePolicy>> findByServiceInstancePolicy(ServiceInstancePolicy policy) {
		LocalDateTime fromDateTime = policy.getOption("from-datetime", LocalDateTime.class);
		String fromDuration = policy.getOption("from-duration", String.class);
		LocalDateTime temporal = null;
		Criteria criteria = null;
		if (fromDateTime != null) {
			temporal = fromDateTime;
		}
		if (fromDuration != null) {
			temporal = LocalDateTime.now().minus(Duration.parse(fromDuration));;
		}
		if (temporal != null) {
			criteria = Criteria.where("bound_applications").isNull().and("last_updated").lessThanOrEquals(temporal);
		} else {
			criteria = Criteria.where("bound_applications").isNull();
		}
		return
			client
				.select()
					.from(ServiceInstanceDetail.tableName())
					.project(ServiceInstanceDetail.columnNames())
					.matching(criteria)
					.orderBy(Order.asc("organization"), Order.asc("space"), Order.asc("service_name"))
				.map((row, metadata) -> fromRow(row))
						.all()
						.map(r -> toTuple(r, policy));
	}

	public Flux<ServiceInstanceDetail> findByDateRange(LocalDate start, LocalDate end) {
		return client
				.select()
					.from(ServiceInstanceDetail.tableName())
					.project(ServiceInstanceDetail.columnNames())
					.matching(Criteria.where("last_updated").lessThanOrEquals(LocalDateTime.of(end, LocalTime.MAX)).and("last_updated").greaterThan(LocalDateTime.of(start, LocalTime.MIDNIGHT)))
					.orderBy(Order.desc("last_updated"))
				.map((row, metadata) -> fromRow(row))
				.all();
	}

	private Tuple2<ServiceInstanceDetail, ServiceInstancePolicy> toTuple(ServiceInstanceDetail detail, ServiceInstancePolicy policy) {
		return Tuples.of(detail, policy);
	}

}