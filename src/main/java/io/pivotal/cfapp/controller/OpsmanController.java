package io.pivotal.cfapp.controller;

import java.util.List;

import org.cloudfoundry.uaa.tokens.GetTokenByPasswordResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import io.pivotal.cfapp.config.OpsmanSettings;
import io.pivotal.cfapp.domain.product.DeployedProduct;
import io.pivotal.cfapp.domain.product.OmInfo;
import io.pivotal.cfapp.domain.product.StemcellAssignments;
import io.pivotal.cfapp.domain.product.StemcellAssociations;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "om.enabled", havingValue = "true")
public class OpsmanController {

    private static final String URI_TEMPLATE = "https://%s%s";
    private final WebClient client;
    private final OpsmanSettings settings;
    private String accessToken;

    @Autowired
    public OpsmanController(
        OpsmanSettings settings,
        WebClient client
    ) {
        this.settings = settings;
        this.client = client;
    }

    @GetMapping("/products/deployed")
    public Mono<ResponseEntity<List<DeployedProduct>>> getDeployedProducts() {
        String uri = String.format(URI_TEMPLATE, settings.getApiHost(), "/api/v0/deployed/products");
        return
            client
                .get()
                .uri(uri)
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                    .bodyToFlux(DeployedProduct.class)
                    .collectList()
                    .map(products -> ResponseEntity.ok(products));
    }

    @GetMapping("/products/stemcell/assignments")
    public Mono<ResponseEntity<StemcellAssignments>> getStemcellAssignments() {
        String uri = String.format(URI_TEMPLATE, settings.getApiHost(), "/api/v0/stemcell_assignments");
        return
            client
                .get()
                .uri(uri)
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                    .bodyToMono(StemcellAssignments.class)
                    .map(assignments -> ResponseEntity.ok(assignments));
    }

    @GetMapping("/products/stemcell/associations")
    public Mono<ResponseEntity<StemcellAssociations>> getStemcellAssociations() {
        String uri = String.format(URI_TEMPLATE, settings.getApiHost(), "/api/v0/stemcell_associations");
        return
            getOmVersion()
            .filter(version -> version.startsWith("2.6"))
            .flatMap(
                r ->
                    client
                        .get()
                        .uri(uri)
                        .headers(h -> h.setBearerAuth(accessToken))
                        .retrieve()
                            .bodyToMono(StemcellAssociations.class)
                            .map(associations -> ResponseEntity.ok(associations))
            );
    }

    @GetMapping("/products/om/info")
    public Mono<ResponseEntity<OmInfo>> getOmInfo() {
        String uri = String.format(URI_TEMPLATE, settings.getApiHost(), "/api/v0/info");
        return
            client
                .get()
                .uri(uri)
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                    .bodyToMono(OmInfo.class)
                    .map(info -> ResponseEntity.ok(info));
    }

    private Mono<String> getOmVersion() {
        String uri = String.format(URI_TEMPLATE, settings.getApiHost(), "/api/v0/info");
        return
            client
                .get()
                .uri(uri)
                .retrieve()
                    .bodyToMono(OmInfo.class)
                    .map(response -> response.getInfo().getVersion());
    }

    // @see https://docs.cloudfoundry.org/api/uaa/version/4.35.0/index.html#password-grant
    @Scheduled(cron = "${cron.accessTokenRefresh}")
    protected void refreshOauthToken() {
        if (accessToken != null) {
            revokeAccessToken()
                .map(r -> getAccessToken().subscribe(result -> accessToken = result));
        } else {
            getAccessToken()
                .subscribe(result -> accessToken = result);
        }
    }

    private Mono<Void> revokeAccessToken() {
        String revoke = String.format(URI_TEMPLATE, settings.getApiHost(), "/uaa/oauth/revoke/client");
        return client
                .get()
                .uri(revoke + "/opsman")
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(Void.class);
    }

    private Mono<String> getAccessToken() {
        String get = String.format(URI_TEMPLATE, settings.getApiHost(), "/uaa/oauth/accessToken");
        LinkedMultiValueMap<String, String> request = new LinkedMultiValueMap<>();
        request.add("grant_type", "password");
        request.add("client_id", "opsman");
        request.add("client_secret", "");
        request.add("username", settings.getUsername());
        request.add("password", settings.getPassword());
        return client
                .post()
                .uri(get)
                .body(request)
                .retrieve()
                .bodyToMono(GetTokenByPasswordResponse.class)
                .map(r -> r.getAccessToken());
    }

}