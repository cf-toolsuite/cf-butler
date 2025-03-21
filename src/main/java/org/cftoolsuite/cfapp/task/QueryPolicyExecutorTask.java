package org.cftoolsuite.cfapp.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import io.r2dbc.spi.ReadableMetadata;
import org.apache.commons.lang3.StringUtils;
import org.cftoolsuite.cfapp.config.PasSettings;
import org.cftoolsuite.cfapp.domain.Defaults;
import org.cftoolsuite.cfapp.domain.EmailAttachment;
import org.cftoolsuite.cfapp.domain.Query;
import org.cftoolsuite.cfapp.domain.QueryPolicy;
import org.cftoolsuite.cfapp.event.EmailNotificationEvent;
import org.cftoolsuite.cfapp.service.PoliciesService;
import org.cftoolsuite.cfapp.service.QueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

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
    public void execute(String id) {
        log.info("QueryPolicyExecutorTask with id={} started", id);
        fetchQueryPolicy(id)
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
                                .carbonCopyRecipients(result.getT1().getEmailNotificationTemplate().getCc())
                                .blindCarbonCopyRecipients(result.getT1().getEmailNotificationTemplate().getBcc())
                                .subject(result.getT1().getEmailNotificationTemplate().getSubject())
                                .body(result.getT1().getEmailNotificationTemplate().getBody())
                                .attachments(buildAttachments(result.getT2()))
                                )
                        );
                log.info("QueryPolicyExecutorTask with id={} completed", id);
            },
            error -> {
                log.error(String.format("QueryPolicyExecutorTask with id=%s terminated with error", id), error);
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

    protected Flux<QueryPolicy> fetchQueryPolicy(String id) {
        return
            policiesService
                .findQueryPolicyById(id)
                .flatMapMany(policy -> Flux.fromIterable(policy.getQueryPolicies()));
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
                List<String> headerRow = new ArrayList<>(tuple.getT1());
                builder.append(String.join(",", headerRow));
            }
            builder.append(System.lineSeparator());
            builder.append(tuple.getT2());
            i++;
        }
        return Mono.just(builder.toString());
    }

    private static Mono<Tuple2<Collection<String>, String>> toCommaSeparatedValue(Tuple2<Row, RowMetadata> tuple) {
        Collection<String> columnNames = tuple.getT2().getColumnMetadatas().stream().map(ReadableMetadata::getName).toList();
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
