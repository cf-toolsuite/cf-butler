package io.pivotal.cfapp.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.data.r2dbc.core.DatabaseClient.GenericInsertSpec;
import org.springframework.data.r2dbc.query.Criteria;
import org.springframework.stereotype.Repository;

import io.pivotal.cfapp.domain.HistoricalRecord;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class R2dbcHistoricalRecordRepository {

	private final DatabaseClient client;

	@Autowired
	public R2dbcHistoricalRecordRepository(DatabaseClient client) {
		this.client = client;
	}

	public Mono<HistoricalRecord> save(HistoricalRecord entity) {
		GenericInsertSpec<Map<String, Object>> spec =
			client.insert().into("historical_record")
				.value("transaction_datetime", entity.getTransactionDateTime());
		spec = spec.value("action_taken", entity.getActionTaken());
		spec = spec.value("organization", entity.getOrganization());
		spec = spec.value("space", entity.getSpace());
		if (entity.getAppId() != null) {
			spec = spec.value("app_id", entity.getAppId());
		} else {
			spec = spec.nullValue("app_id");
		}
		if (entity.getServiceInstanceId() != null) {
			spec = spec.value("service_instance_id", entity.getServiceInstanceId());
		} else {
			spec = spec.nullValue("service_instance_id");
		}
		spec = spec.value("type", entity.getType());
		if (entity.getName() != null) {
			spec = spec.value("name", entity.getName());
		} else {
			spec = spec.nullValue("name");
		}
		return spec.fetch().rowsUpdated().then(Mono.just(entity));
	}

	public Flux<HistoricalRecord> findAll() {
		return client
				.select()
				.from(HistoricalRecord.tableName())
				.project(HistoricalRecord.columnNames())
				.orderBy(Order.desc("transaction_datetime"))
				.as(HistoricalRecord.class)
				.fetch()
				.all();
	}

	public Flux<HistoricalRecord> findByDateRange(LocalDate start, LocalDate end) {
		return client
				.select()
				.from(HistoricalRecord.tableName())
				.project(HistoricalRecord.columnNames())
				.matching(Criteria.where("transaction_datetime").lessThanOrEquals(LocalDateTime.of(end, LocalTime.MAX)).and("transaction_datetime").greaterThan(LocalDateTime.of(start, LocalTime.MIDNIGHT)))
				.orderBy(Order.desc("transaction_datetime"))
				.as(HistoricalRecord.class)
				.fetch()
				.all();
	}

}
