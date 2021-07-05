package io.pivotal.cfapp.client;

import org.cloudfoundry.uaa.tokens.GetTokenByPasswordResponse;
import org.cloudfoundry.uaa.tokens.GetTokenByClientCredentialsResponse;
import org.cloudfoundry.uaa.tokens.GrantType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import io.pivotal.cfapp.config.OpsmanSettings;
import reactor.core.publisher.Mono;

@Component
class OpsmanAccessTokenProvider {

    private static final String URI_TEMPLATE = "https://%s%s";
    private final WebClient client;
    private final OpsmanSettings settings;

    @Autowired
    public OpsmanAccessTokenProvider(
            WebClient client,
            OpsmanSettings settings
            ) {
        this.client = client;
        this.settings = settings;
    }

    // @see https://docs.cloudfoundry.org/api/uaa/version/4.35.0/index.html
    Mono<String> obtainAccessToken() {
        String get = String.format(URI_TEMPLATE, settings.getApiHost(), "/uaa/oauth/token");
        LinkedMultiValueMap<String, String> request = new LinkedMultiValueMap<>();
        request.add("grant_type", settings.getGrantType().getValue());
        request.add("client_id", settings.getClientId());
        request.add("client_secret", settings.getClientSecret());
        request.add("username", settings.getUsername());
        request.add("password", settings.getPassword());
        Mono<String> response = null;
        switch(settings.getGrantType()) {
            case PASSWORD:
                response = 
                    client
                        .post()
                        .uri(get)
                        .bodyValue(request)
                        .retrieve()
                        .bodyToMono(GetTokenByPasswordResponse.class)
                        .map(GetTokenByPasswordResponse::getAccessToken);
                break;
            case CLIENT_CREDENTIALS:
                response = 
                    client
                        .post()
                        .uri(get)
                        .bodyValue(request)
                        .retrieve()
                        .bodyToMono(GetTokenByClientCredentialsResponse.class)
                        .map(GetTokenByClientCredentialsResponse::getAccessToken);
                break;
            default: 
                response = 
                    client
                        .post()
                        .uri(get)
                        .bodyValue(request)
                        .retrieve()
                        .bodyToMono(GetTokenByPasswordResponse.class)
                        .map(GetTokenByPasswordResponse::getAccessToken);
                break;
        }
        return response;
    }

    Mono<Void> revokeAccessToken(String bearerToken) {
        String revoke = String.format(URI_TEMPLATE, settings.getApiHost(), "/uaa/oauth/revoke/client");
        return
            client
                .delete()
                .uri(revoke + "/" + settings.getClientId())
                .headers(h -> h.setBearerAuth(bearerToken))
                .retrieve()
                .bodyToMono(Void.class);
    }

}
