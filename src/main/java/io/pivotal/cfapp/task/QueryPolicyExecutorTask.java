package io.pivotal.cfapp.task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.domain.Defaults;
import io.pivotal.cfapp.domain.Query;
import io.pivotal.cfapp.domain.QueryPolicy;
import io.pivotal.cfapp.event.EmailNotificationEvent;
import io.pivotal.cfapp.service.PoliciesService;
import io.pivotal.cfapp.service.QueryService;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Slf4j
@Component
public class QueryPolicyExecutorTask implements PolicyExecutorTask {

    private final PoliciesService policiesService;
    private final QueryService queryService;
    private final ApplicationEventPublisher publisher;

    @Autowired
    public QueryPolicyExecutorTask(
        PoliciesService policiesService,
        QueryService queryService,
        ApplicationEventPublisher publisher
    ) {
        this.policiesService = policiesService;
        this.queryService = queryService;
        this.publisher = publisher;
    }

	@Override
    public void execute() {
		log.info("QueryPolicyExecutorTask started");
        fetchQueryPolicies()
            .concatMap(qp -> executeQueries(qp).collectList().map(result -> Tuples.of(qp, result)))
            .collectList()
	    	.subscribe(
                results -> {
                    results.forEach(
                        result ->
                            publisher.publishEvent(
                                new EmailNotificationEvent(this)
                                    .from(result.getT1().getEmailNotificationTemplate().getFrom())
                                    .recipients(result.getT1().getEmailNotificationTemplate().getTo())
                                    .subject(result.getT1().getEmailNotificationTemplate().getSubject())
                                    .body(result.getT1().getEmailNotificationTemplate().getBody())
                                    .attachmentContents(toMap(result.getT2()))
                            )
                    );
					log.info("QueryPolicyExecutorTask completed");
					log.info("-- {} query policies executed.", results.size());
				},
				error -> {
					log.error("QueryPolicyExecutorTask terminated with error", error);
				}
			);
    }

    @Scheduled(cron = "${cron.execution}")
    protected void runTask() {
    	execute();
    }

	protected Flux<QueryPolicy> fetchQueryPolicies() {
        return
            policiesService
				.findAllQueryPolicies()
                .flatMapMany(policy -> Flux.fromIterable(policy.getQueryPolicies()));
    }

    protected Flux<Tuple2<String, String>> executeQueries(QueryPolicy policy) {
        return Flux
                .fromIterable(policy.getQueries())
                .concatMap(q -> executeQuery(q).map(result -> Tuples.of(q.getName(), result)));
    }

    protected Mono<String> executeQuery(Query query) {
        return queryService
                .executeQuery(query)
                .flatMap(tuple -> toCommaSeparatedValue(tuple))
                .collectList()
                .map(list -> String.join(System.getProperty("line.separator"), list));
    }

    private static Mono<String> toCommaSeparatedValue(Tuple2<Row, RowMetadata> tuple) {
        List<String> rawValueList =
            tuple
                .getT2()
                    .getColumnNames()
                        .stream()
                            .map(column -> Defaults.getValueOrDefault(tuple.getT1().get(column).toString(), ""))
                            .collect(Collectors.toList());
        return Mono.just(String.join(",", rawValueList));
    }

    private static Map<String, String> toMap(List<Tuple2<String, String>> contents) {
        Map<String, String> result = new HashMap<>();
        for (Tuple2<String, String> c: contents) {
            result.put(c.getT1(), c.getT2());
        }
        return result;
    }
}
