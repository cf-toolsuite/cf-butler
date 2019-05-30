package io.pivotal.cfapp.client;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import io.pivotal.cfapp.config.OpsmanSettings;
import io.pivotal.cfapp.domain.product.DeployedProduct;
import io.pivotal.cfapp.domain.product.OmInfo;
import io.pivotal.cfapp.domain.product.StemcellAssignments;
import reactor.core.publisher.Mono;

@Component
public class OpsmanClient {

    private static final String URI_TEMPLATE = "https://%s%s";
    private final WebClient client;
    private final OpsmanSettings settings;

    @Autowired
    public OpsmanClient(
        WebClient client,
        OpsmanSettings settings
    ) {
        this.client = client;
        this.settings = settings;
    }

    public Mono<List<DeployedProduct>> getDeployedProducts(String uaaToken) {
        String uri = String.format(URI_TEMPLATE, settings.getApiHost(), "/api/v0/deployed/products");
        return client
                .get()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, uaaToken)
                .retrieve()
                    .bodyToFlux(DeployedProduct.class)
                    .collectList();
    }

    public Mono<Void> logoutAllActiveUsers(String uaaToken) {
        String uri = String.format(URI_TEMPLATE, settings.getApiHost(), "/api/v0/sessions");
        return client
                .delete()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, uaaToken)
                .retrieve()
                    .bodyToMono(Void.class);
    }

    public Mono<StemcellAssignments> getStemcellAssignments(String uaaToken) {
        String uri = String.format(URI_TEMPLATE, settings.getApiHost(), "/api/v0/stemcell_assignments");
        return client
                .get()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, uaaToken)
                    .retrieve()
                        .bodyToMono(StemcellAssignments.class);
    }

    public Mono<OmInfo> getOmInfo(String uaaToken) {
        String uri = String.format(URI_TEMPLATE, settings.getApiHost(), "/api/v0/info");
        return client
                .get()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, uaaToken)
                    .retrieve()
                        .bodyToMono(OmInfo.class);
    }

}