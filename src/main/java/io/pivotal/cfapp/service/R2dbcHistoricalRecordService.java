package io.pivotal.cfapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.pivotal.cfapp.domain.HistoricalRecord;
import io.pivotal.cfapp.repository.R2dbcHistoricalRecordRepository;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class R2dbcHistoricalRecordService implements HistoricalRecordService {

	private final R2dbcHistoricalRecordRepository repo;

	@Autowired
	public R2dbcHistoricalRecordService(R2dbcHistoricalRecordRepository repo) {
		this.repo = repo;
	}

	@Override
	public Mono<HistoricalRecord> save(HistoricalRecord entity) {
		return repo
				.save(entity)
				.onErrorContinue(
					(ex, data) -> log.error("Problem saving historical record {}.", entity, ex));
	}

	@Override
	public Flux<HistoricalRecord> findAll() {
		return repo.findAll();
	}

}
