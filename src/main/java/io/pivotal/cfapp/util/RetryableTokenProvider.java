package io.pivotal.cfapp.util;

import java.time.Duration;

import org.cloudfoundry.reactor.DefaultConnectionContext;
import org.cloudfoundry.reactor.TokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

public class RetryableTokenProvider {

    public static Mono<String> getToken(TokenProvider provider, DefaultConnectionContext connectionContext) {
        return Mono.defer(() ->
                provider.getToken(connectionContext)
                    .onErrorResume(WebClientResponseException.class, throwable -> {
                        if (throwable.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                            provider.invalidate(connectionContext);
                            // Retry logic
                            return Mono.error(throwable); // Propagate error to trigger retry
                        }
                        return Mono.error(throwable); // Propagate other errors
                    })
                    .retryWhen(Retry
                        .backoff(2, Duration.ofSeconds(2))
                    )
        );
    }
}
