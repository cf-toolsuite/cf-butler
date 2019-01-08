package io.pivotal.cfapp.repository;

import org.davidmoten.rx.jdbc.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import io.pivotal.cfapp.domain.HistoricalRecord;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("jdbc")
@Repository
public class JdbcHistoricalRecordRepository {

	private Database database;

	@Autowired
	public JdbcHistoricalRecordRepository(Database database) {
		this.database = database;
	}

	public Mono<HistoricalRecord> save(HistoricalRecord entity) {
		String createOne = "insert into historical_record (dateTimeRemoved, organization, space, id, type, name, status, error_details) values (?, ?, ?, ?, ?, ?, ?, ?)";
		Flux.from(database
					.update(createOne)
					.parameters(
						entity.getDateTimeRemoved(),
						entity.getOrganization(),
						entity.getSpace(),
						entity.getId(),
						entity.getType(),
						entity.getName(),
						entity.getStatus(),
						entity.getErrorDetails()
					)
					.counts())
			.subscribe();

		return Mono.just(entity);
	}
}
