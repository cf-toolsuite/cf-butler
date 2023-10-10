package io.pivotal.cfapp.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.config.PasSettings;
import io.pivotal.cfapp.domain.Defaults;
import io.pivotal.cfapp.domain.EmailAttachment;
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

    private final PasSettings settings;
    private final PoliciesService policiesService;
    private final QueryService queryService;
    private final ApplicationEventPublisher publisher;

    @Autowired
    public QueryPolicyExecutorTask(
            PasSettings settings,
            PoliciesService policiesService,
            QueryService queryService,
            ApplicationEventPublisher publisher
            ) {
        this.settings = settings;
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
                                .domain(settings.getAppsDomain())
                                .from(result.getT1().getEmailNotificationTemplate().getFrom())
                                .recipients(result.getT1().getEmailNotificationTemplate().getTo())
                                .subject(result.getT1().getEmailNotificationTemplate().getSubject())
                                .body(result.getT1().getEmailNotificationTemplate().getBody())
                                .attachments(buildAttachments(result.getT2()))
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

    protected Flux<Tuple2<String, String>> executeQueries(QueryPolicy policy) {
        return Flux
                .fromIterable(policy.getQueries())
                .concatMap(q -> executeQuery(q).map(result -> Tuples.of(q.getName(), result)));
    }

    protected Mono<String> executeQuery(Query query) {
        Flux<Tuple2<Row, RowMetadata>> results =
                queryService
                .executeQuery(query);
        return results
                .flatMap(QueryPolicyExecutorTask::toCommaSeparatedValue)
                .collectList()
                .flatMap(QueryPolicyExecutorTask::constructCsvOutput);
    }

    protected Flux<QueryPolicy> fetchQueryPolicies() {
        return
                policiesService
                .findAllQueryPolicies()
                .flatMapMany(policy -> Flux.fromIterable(policy.getQueryPolicies()));
    }

    @Scheduled(cron = "${cron.execution}")
    protected void runTask() {
        execute();
    }

    private static List<EmailAttachment> buildAttachments(List<Tuple2<String, String>> tuples) {
        List<EmailAttachment> result = new ArrayList<>();
        for (Tuple2<String, String> t: tuples) {
            if (StringUtils.isNotBlank(t.getT2())) {
                result.add(
                        EmailAttachment
                        .builder()
                        .filename(t.getT1())
                        .content(t.getT2())
                        .extension(".csv")
                        .mimeType("text/plain")
                        .build()
                        );
            } else {
                result.add(
                        EmailAttachment
                        .builder()
                        .filename(t.getT1())
                        .content("No results.")
                        .extension(".csv")
                        .mimeType("text/plain")
                        .build()
                        );
            }
        }
        return result;
    }

    private static Mono<String> constructCsvOutput(List<Tuple2<Collection<String>, String>> columnNamesAndRows) {
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (Tuple2<Collection<String>, String> tuple: columnNamesAndRows) {
            if (i == 0) {
                List<String> headerRow = new ArrayList<>();
                headerRow.addAll(tuple.getT1());
                builder.append(String.join(",", headerRow));
            }
            builder.append(System.getProperty("line.separator"));
            builder.append(tuple.getT2());
            i++;
        }
        return Mono.just(builder.toString());
    }

    private static Mono<Tuple2<Collection<String>, String>> toCommaSeparatedValue(Tuple2<Row, RowMetadata> tuple) {
        Collection<String> columnNames = tuple.getT2().getColumnMetadatas().stream().map(columnName -> columnName.getName()).toList();
        List<String> rawValueList =
            columnNames
                .stream()
                .map(columnName -> wrap(Defaults.getColumnValueOrDefault(tuple.getT1(), columnName, "").toString()))
                .collect(Collectors.toList());
        return Mono.just(Tuples.of(columnNames, String.join(",", rawValueList)));
    }

    private static String wrap(String value) {
        return value != null ? StringUtils.wrap(value, '"') : StringUtils.wrap("", '"');
    }
}
