package io.pivotal.cfapp.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.data.relational.core.query.Criteria;
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
		return
			client
				.insert()
				.into(HistoricalRecord.class)
				.table(HistoricalRecord.tableName())
				.using(entity)
				.fetch()
				.rowsUpdated()
				.then(Mono.just(entity));
	}

	public Flux<HistoricalRecord> findAll() {
		return client
				.select()
				.from(HistoricalRecord.tableName())
				.project(HistoricalRecord.columnNames())
				.orderBy(Order.desc("transaction_date_time"))
				.as(HistoricalRecord.class)
				.fetch()
				.all();
	}

	public Flux<HistoricalRecord> findByDateRange(LocalDate start, LocalDate end) {
		return client
				.select()
				.from(HistoricalRecord.tableName())
				.project(HistoricalRecord.columnNames())
				.matching(Criteria.where("transaction_date_time").lessThanOrEquals(LocalDateTime.of(end, LocalTime.MAX)).and("transaction_date_time").greaterThan(LocalDateTime.of(start, LocalTime.MIDNIGHT)))
				.orderBy(Order.desc("transaction_date_time"))
				.as(HistoricalRecord.class)
				.fetch()
				.all();
	}

}
