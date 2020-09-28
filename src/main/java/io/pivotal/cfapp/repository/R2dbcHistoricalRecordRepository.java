package io.pivotal.cfapp.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;

import io.pivotal.cfapp.domain.HistoricalRecord;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class R2dbcHistoricalRecordRepository {

	private final R2dbcEntityOperations client;

	@Autowired
	public R2dbcHistoricalRecordRepository(R2dbcEntityOperations client) {
		this.client = client;
	}

	public Mono<HistoricalRecord> save(HistoricalRecord entity) {
		return
			client
				.insert(entity);
	}

	public Flux<HistoricalRecord> findAll() {
		return 
			client
				.select(HistoricalRecord.class)
					.matching(Query.empty().sort(Sort.by(Order.desc("transaction_date_time"))))
					.all();
	}

	public Flux<HistoricalRecord> findByDateRange(LocalDate start, LocalDate end) {
		Criteria criteria =
			Criteria.where("transaction_date_time").lessThanOrEquals(LocalDateTime.of(end, LocalTime.MAX)).and("transaction_date_time").greaterThan(LocalDateTime.of(start, LocalTime.MIDNIGHT));
		return 
			client
				.select(HistoricalRecord.class)
					.matching(Query.query(criteria).sort(Sort.by(Order.desc("transaction_date_time"))))
					.all();
	}

}
