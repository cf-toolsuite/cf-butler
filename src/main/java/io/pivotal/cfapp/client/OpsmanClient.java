package io.pivotal.cfapp.client;

import java.time.Duration;
import java.util.List;

import org.cloudfoundry.reactor.DefaultConnectionContext;
import org.cloudfoundry.reactor.TokenProvider;
import org.cloudfoundry.reactor.tokenprovider.PasswordGrantTokenProvider;
import org.cloudfoundry.reactor.uaa.ReactorUaaClient;
import org.cloudfoundry.uaa.tokens.GetTokenByPasswordRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import io.pivotal.cfapp.config.OpsmanSettings;
import io.pivotal.cfapp.domain.product.DeployedProduct;
import io.pivotal.cfapp.domain.product.OmInfo;
import io.pivotal.cfapp.domain.product.StemcellAssignments;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@ConditionalOnProperty(name = "om.enabled", havingValue = "true")
public class OpsmanClient {

    private static final String URI_TEMPLATE = "https://%s%s";
    private final WebClient client;
    private final OpsmanSettings settings;
    private final ReactorUaaClient uaaClient;

    @Autowired
    public OpsmanClient(
        WebClient client,
        OpsmanSettings settings
    ) {
        this.client = client;
        this.settings = settings;

        DefaultConnectionContext connectionContext =
            DefaultConnectionContext
                .builder()
                    .apiHost(settings.getApiHost())
                    .skipSslValidation(settings.isSslValidationSkipped())
                    .connectTimeout(Duration.ofMinutes(3))
                    .build();

        TokenProvider tokenProvider =
            PasswordGrantTokenProvider
                .builder()
                    .username(settings.getUsername())
                    .password(settings.getPassword())
                    .build();

        this.uaaClient = ReactorUaaClient
                .builder()
                    .connectionContext(connectionContext)
                    .tokenProvider(tokenProvider)
                    .build();
    }


    public Mono<List<DeployedProduct>> getDeployedProducts() {
        String uri = String.format(URI_TEMPLATE, settings.getApiHost(), "/api/v0/deployed/products");
        return getOauthToken()
                .flatMap(authToken ->
                    client
                        .get()
                        .uri(uri)
                        .header(HttpHeaders.AUTHORIZATION, authToken)
                        .retrieve()
                            .bodyToFlux(DeployedProduct.class)
                            .collectList());
    }

    public Mono<Void> logoutAllActiveUsers() {
        String uri = String.format(URI_TEMPLATE, settings.getApiHost(), "/api/v0/sessions");
        return getOauthToken()
                .flatMap(authToken ->
                    client
                        .delete()
                        .uri(uri)
                        .header(HttpHeaders.AUTHORIZATION, authToken)
                        .retrieve()
                            .bodyToMono(Void.class));
    }

    public Mono<StemcellAssignments> getStemcellAssignments() {
        String uri = String.format(URI_TEMPLATE, settings.getApiHost(), "/api/v0/stemcell_assignments");
        return getOauthToken()
                .flatMap(authToken ->
                    client
                        .get()
                        .uri(uri)
                        .header(HttpHeaders.AUTHORIZATION, authToken)
                            .retrieve()
                                .bodyToMono(StemcellAssignments.class));
    }

    public Mono<OmInfo> getOmInfo() {
        String uri = String.format(URI_TEMPLATE, settings.getApiHost(), "/api/v0/info");
        return getOauthToken()
                .flatMap(authToken ->
                    client
                        .get()
                        .uri(uri)
                        .header(HttpHeaders.AUTHORIZATION, authToken)
                            .retrieve()
                                .bodyToMono(OmInfo.class));
    }

    private Mono<String> getOauthToken() {
        return uaaClient
                .tokens()
                    .getByPassword(GetTokenByPasswordRequest
                                    .builder()
                                        .clientId("opsman")
                                        .clientSecret("")
                                        .username(settings.getUsername())
                                        .password(settings.getPassword())
                                        .build())
                    .map(r -> {
                        log.info(String.format("%s %s", "Bearer", r.getAccessToken()));
                        return r.getAccessToken();
                    });

    }

}