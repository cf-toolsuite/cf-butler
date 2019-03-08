package io.pivotal.cfapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.pivotal.cfapp.domain.HistoricalRecord;
import io.pivotal.cfapp.repository.R2dbcHistoricalRecordRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class R2dbcHistoricalRecordService implements HistoricalRecordService {

	private final R2dbcHistoricalRecordRepository repo;

	@Autowired
	public R2dbcHistoricalRecordService(R2dbcHistoricalRecordRepository repo) {
		this.repo = repo;
	}

	@Override
	public Mono<HistoricalRecord> save(HistoricalRecord entity) {
		return repo.save(entity);
	}

	@Override
	public Flux<HistoricalRecord> findAll() {
		return repo.findAll();
	}

}
