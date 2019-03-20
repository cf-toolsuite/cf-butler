package io.pivotal.cfapp.repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.function.DatabaseClient;
import org.springframework.data.r2dbc.function.DatabaseClient.GenericInsertSpec;
import org.springframework.stereotype.Repository;

import io.pivotal.cfapp.config.DbmsSettings;
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
	private final DbmsSettings settings;

	@Autowired
	public R2dbcServiceInstanceDetailRepository(
		DatabaseClient client,
		DbmsSettings settings) {
		this.client = client;
		this.settings = settings;
	}

	public Mono<ServiceInstanceDetail> save(ServiceInstanceDetail entity) {
		GenericInsertSpec<Map<String, Object>> spec = client.insert().into("service_instance_detail")
				.value("organization", entity.getOrganization());
		spec = spec.value("space", entity.getSpace());
		spec = spec.value("service_id", entity.getServiceId());
		if (entity.getName() != null) {
			spec = spec.value("service_name", entity.getName());
		} else {
			spec = spec.nullValue("service_name", String.class);
		}
		if (entity.getService() != null) {
			spec = spec.value("service", entity.getService());
		} else {
			spec = spec.nullValue("service", String.class);
		}
		if (entity.getDescription() != null) {
			spec = spec.value("description", entity.getDescription());
		} else {
			spec = spec.nullValue("description", String.class);
		}
		if (entity.getPlan() != null) {
			spec = spec.value("plan", entity.getPlan());
		} else {
			spec = spec.nullValue("plan", String.class);
		}
		if (entity.getType() != null) {
			spec = spec.value("type", entity.getType());
		} else {
			spec = spec.nullValue("type", String.class);
		}
		if (entity.getApplications() != null) {
			spec = spec.value("bound_applications", String.join(",", entity.getApplications()));
		} else {
			spec = spec.nullValue("bound_applications", String.class);
		}
		if (entity.getLastOperation() != null) {
			spec = spec.value("last_operation", entity.getLastOperation());
		} else {
			spec = spec.nullValue("last_operation", String.class);
		}
		if (entity.getLastUpdated() != null) {
			spec = spec.value("last_updated", Timestamp.valueOf(entity.getLastUpdated()));
		} else {
			spec = spec.nullValue("last_updated", Timestamp.class);
		}
		if (entity.getDashboardUrl() != null) {
			spec = spec.value("dashboard_url", entity.getDashboardUrl());
		} else {
			spec = spec.nullValue("dashboard_url", String.class);
		}
		if (entity.getRequestedState() != null) {
			spec = spec.value("requested_state", entity.getRequestedState());
		} else {
			spec = spec.nullValue("requested_state", String.class);
		}
		return spec.fetch().rowsUpdated().then(Mono.just(entity));
	}

	public Flux<ServiceInstanceDetail> findAll() {
		return client.select().from("service_instance_detail")
				.orderBy(Sort.by("organization", "space", "service", "service_name"))
				.map((row, metadata) -> fromRow(row))
				.all();
	}

	private ServiceInstanceDetail fromRow(Row row) {
		return ServiceInstanceDetail
				.builder()
					.pk(row.get("pk", Long.class))
					.organization(row.get("organization", String.class))
					.space(row.get("space", String.class))
					.name(row.get("service_name", String.class))
					.service(row.get("service", String.class))
					.description(row.get("description", String.class)).type(row.get("type", String.class))
					.plan(row.get("plan", String.class))
					.applications(row.get("bound_applications", String.class) != null
							? Arrays.asList(row.get("bound_applications", String.class).split("\\s*,\\s*"))
							: null)
					.lastOperation(row.get("last_operation", String.class))
					.lastUpdated(row.get("last_updated", Timestamp.class) != null
							? row.get("last_updated", Timestamp.class).toLocalDateTime()
							: null)
					.dashboardUrl(row.get("dashboard_url", String.class))
					.requestedState(row.get("requested_state", String.class))
					.build();
	}

	public Mono<Void> deleteAll() {
		return client.execute().sql("delete from service_instance_detail")
						.fetch()
						.rowsUpdated()
						.then();
	}

	public Flux<Tuple2<ServiceInstanceDetail, ServiceInstancePolicy>> findByServiceInstancePolicy(ServiceInstancePolicy policy) {
		String index = settings.getBindPrefix() + 1;
		String select = "select pk, organization, space, service_id, service_name, service, description, plan, type, bound_applications, last_operation, last_updated, dashboard_url, requested_state";
		String from = "from service_instance_detail";
		StringBuilder where = new StringBuilder();
		Timestamp temporal = null;
		where.append("where bound_applications is null "); // orphans only
		if (policy.getFromDateTime() != null) {
			where.append("and last_updated <= " + index + " ");
			temporal = Timestamp.valueOf(policy.getFromDateTime());
		}
		if (policy.getFromDuration() != null) {
			where.append("and last_updated <= " + index + " ");
			LocalDateTime eventTime = LocalDateTime.now().minus(policy.getFromDuration());
			temporal = Timestamp.valueOf(eventTime);
		}
		String orderBy = "order by organization, space, service_name";
		String sql = String.join(" ", select, from, where, orderBy);
		return client.execute().sql(sql)
						.bind(index, temporal)
						.map((row, metadata) -> fromRow(row))
						.all()
						.map(r -> toTuple(r, policy));
	}

	public Flux<ServiceInstanceDetail> findByDateRange(LocalDate start, LocalDate end) {
		String sql = "select pk, organization, space, service_id, service_name, service, description, plan, type, bound_applications, last_operation, last_updated, dashboard_url, requested_state from service_instance_detail where last_updated <= " + settings.getBindPrefix() + 2 + " and last_updated > " + settings.getBindPrefix() + 1 + " order by last_updated desc";
		return client.execute().sql(sql)
				.bind(settings.getBindPrefix() + 1, Timestamp.valueOf(LocalDateTime.of(end, LocalTime.MAX)))
				.bind(settings.getBindPrefix() + 2, Timestamp.valueOf(LocalDateTime.of(start, LocalTime.MIDNIGHT)))
				.as(ServiceInstanceDetail.class)
				.fetch()
				.all();
	}

	private Tuple2<ServiceInstanceDetail, ServiceInstancePolicy> toTuple(ServiceInstanceDetail detail, ServiceInstancePolicy policy) {
		return Tuples.of(detail, policy);
	}

}