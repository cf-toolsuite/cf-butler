package io.pivotal.cfapp.client;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import io.pivotal.cfapp.config.OpsmanSettings;
import io.pivotal.cfapp.domain.product.DeployedProduct;
import io.pivotal.cfapp.domain.product.OmInfo;
import io.pivotal.cfapp.domain.product.StemcellAssignments;
import io.pivotal.cfapp.domain.product.StemcellAssociations;
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

    public Mono<Double> getOmMajorMinorVersion() {
        String uri = String.format(URI_TEMPLATE, settings.getApiHost(), "/api/v0/info");
        return
            client
                .get()
                .uri(uri)
                .retrieve()
                .bodyToMono(OmInfo.class)
                .map(response -> response.getMajorMinorVersion());
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
            getOmMajorMinorVersion()
                .filter(version -> version >= 2.6)
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
