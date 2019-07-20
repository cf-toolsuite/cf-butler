package io.pivotal.cfapp.controller;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import io.pivotal.cfapp.config.OpsmanSettings;
import io.pivotal.cfapp.domain.product.DeployedProduct;
import io.pivotal.cfapp.domain.product.OmInfo;
import io.pivotal.cfapp.domain.product.StemcellAssignments;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "om.enabled", havingValue = "true")
public class OpsmanController {

    private static final String ID = "opsman";
    private static final String URI_TEMPLATE = "https://%s%s";
    private final WebClient client;
    private final OpsmanSettings settings;

    @Autowired
    public OpsmanController(
        ReactiveClientRegistrationRepository clientRegistrationRepository,
		ServerOAuth2AuthorizedClientRepository authorizedClientRepository,
        OpsmanSettings settings
    ) {
        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth =
				new ServerOAuth2AuthorizedClientExchangeFilterFunction(clientRegistrationRepository, authorizedClientRepository);
		oauth.setDefaultOAuth2AuthorizedClient(true);
        this.client =
            WebClient
                .builder()
                    .filter(oauth)
                    .build();
        this.settings = settings;
    }

    @GetMapping("/products/deployed")
    public Mono<ResponseEntity<List<DeployedProduct>>> getDeployedProducts() {
        String uri = String.format(URI_TEMPLATE, settings.getApiHost(), "/api/v0/deployed/products");
        return
            client
                .get()
                .uri(uri)
                .attributes(clientRegistrationId(ID))
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
                .attributes(clientRegistrationId(ID))
                .retrieve()
                    .bodyToMono(StemcellAssignments.class)
                    .map(assignments -> ResponseEntity.ok(assignments));
    }

    @GetMapping("/products/om/info")
    public Mono<ResponseEntity<OmInfo>> getOmInfo() {
        String uri = String.format(URI_TEMPLATE, settings.getApiHost(), "/api/v0/info");
        return
            client
                .get()
                .uri(uri)
                .attributes(clientRegistrationId(ID))
                .retrieve()
                    .bodyToMono(OmInfo.class)
                    .map(info -> ResponseEntity.ok(info));
    }

}