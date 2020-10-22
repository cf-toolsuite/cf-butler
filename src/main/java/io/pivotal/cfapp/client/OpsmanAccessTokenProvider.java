package io.pivotal.cfapp.client;

import org.cloudfoundry.uaa.tokens.GetTokenByPasswordResponse;
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

    // @see https://docs.cloudfoundry.org/api/uaa/version/4.35.0/index.html#password-grant
    Mono<String> obtainAccessToken() {
        String get = String.format(URI_TEMPLATE, settings.getApiHost(), "/uaa/oauth/token");
        LinkedMultiValueMap<String, String> request = new LinkedMultiValueMap<>();
        request.add("grant_type", "password");
        request.add("client_id", "opsman");
        request.add("client_secret", "");
        request.add("username", settings.getUsername());
        request.add("password", settings.getPassword());
        return client
                .post()
                .uri(get)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GetTokenByPasswordResponse.class)
                .map(GetTokenByPasswordResponse::getAccessToken);
    }

    Mono<Void> revokeAccessToken(String existingToken) {
        String revoke = String.format(URI_TEMPLATE, settings.getApiHost(), "/uaa/oauth/revoke/client");
        return
                client
                .get()
                .uri(revoke + "/opsman")
                .headers(h -> h.setBearerAuth(existingToken))
                .retrieve()
                .bodyToMono(Void.class);
    }

}
