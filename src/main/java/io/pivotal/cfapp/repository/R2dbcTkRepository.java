package io.pivotal.cfapp.repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.springframework.data.r2dbc.function.DatabaseClient;
import org.springframework.stereotype.Repository;

import io.pivotal.cfapp.domain.Defaults;
import io.r2dbc.spi.Row;
import reactor.core.publisher.Mono;

@Repository
public class R2dbcTkRepository {

    private final DatabaseClient client;

    public R2dbcTkRepository(DatabaseClient client) {
        this.client = client;
    }

    public Mono<Integer> save() {
        LocalDateTime collectionTime = LocalDateTime.now();
        return client
                .insert().into("time_keeper")
                .value("collection_time", Timestamp.valueOf(collectionTime))
                .fetch().rowsUpdated();
    }

    public Mono<Void> deleteOne() {
		return client.execute().sql("delete from time_keeper")
						.fetch()
						.rowsUpdated()
						.then();
    }

    public Mono<LocalDateTime> findOne() {
		String select = "select collection_time from time_keeper";
		return client.execute().sql(select)
						.map((row, metadata) -> fromRow(row))
						.one();
    }

    private LocalDateTime fromRow(Row row) {
        return toLocalDateTime(Defaults.getValueOrDefault(row.get("collection_time", Timestamp.class), null));
    }

    private LocalDateTime toLocalDateTime(Timestamp t) {
		return t != null ? t.toLocalDateTime(): null;
	}
}