package org.cftoolsuite.cfapp.client;

import java.util.List;

import org.cftoolsuite.cfapp.config.OpsmanSettings;
import org.cftoolsuite.cfapp.domain.product.DeployedProduct;
import org.cftoolsuite.cfapp.domain.product.OmInfo;
import org.cftoolsuite.cfapp.domain.product.StemcellAssignments;
import org.cftoolsuite.cfapp.domain.product.StemcellAssociations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Component
@ConditionalOnProperty(name = "om.enabled", havingValue = "true")
public class OpsmanClient {

    private static final String URI_TEMPLATE = "https://%s%s";
    private final WebClient client;
    private final OpsmanSettings settings;
    private final OpsmanAccessTokenProvider tokenProvider;

    @Autowired
    public OpsmanClient(
            WebClient client,
            OpsmanSettings settings,
            OpsmanAccessTokenProvider tokenProvider
            ) {
        this.client = client;
        this.settings = settings;
        this.tokenProvider = tokenProvider;
    }

    public Mono<List<DeployedProduct>> getDeployedProducts() {
        String uri = String.format(URI_TEMPLATE, settings.getApiHost(), "/api/v0/deployed/products");
        return
            tokenProvider
                .obtainAccessToken()
                .flatMap(token ->
                    client
                        .get()
                        .uri(uri)
                        .headers(h -> h.setBearerAuth(token))
                        .retrieve()
                        .bodyToFlux(DeployedProduct.class)
                        .collectList()
                );
    }

    public Mono<OmInfo> getOmInfo() {
        String uri = String.format(URI_TEMPLATE, settings.getApiHost(), "/api/v0/info");
        return
            tokenProvider
                .obtainAccessToken()
                .flatMap(token ->
                    client
                        .get()
                        .uri(uri)
                        .headers(h -> h.setBearerAuth(token))
                        .retrieve()
                        .bodyToMono(OmInfo.class)
                );
    }

    public Mono<String> getOmVersion() {
        String uri = String.format(URI_TEMPLATE, settings.getApiHost(), "/api/v0/info");
        return
            client
                .get()
                .uri(uri)
                .retrieve()
                .bodyToMono(OmInfo.class)
                .map(response -> response.getInfo().getVersion());
    }

    public Mono<StemcellAssignments> getStemcellAssignments() {
        String uri = String.format(URI_TEMPLATE, settings.getApiHost(), "/api/v0/stemcell_assignments");
        return
            tokenProvider
                .obtainAccessToken()
                .flatMap(token ->
                    client
                        .get()
                        .uri(uri)
                        .headers(h -> h.setBearerAuth(token))
                        .retrieve()
                        .bodyToMono(StemcellAssignments.class)
                );
    }

    public Mono<StemcellAssociations> getStemcellAssociations() {
        String uri = String.format(URI_TEMPLATE, settings.getApiHost(), "/api/v0/stemcell_associations");
        return
            getOmInfo()
                .filter(info -> info.getMajorVersion() >= 2 && info.getMinorVersion() >= 6)
                .flatMap(r ->
                    tokenProvider
                        .obtainAccessToken()
                        .flatMap(token ->
                            client
                                .get()
                                .uri(uri)
                                .headers(h -> h.setBearerAuth(token))
                                .retrieve()
                                .bodyToMono(StemcellAssociations.class)
                        )
                );
    }
}
