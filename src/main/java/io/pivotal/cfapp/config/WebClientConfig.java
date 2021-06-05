package io.pivotal.cfapp.config;

import javax.net.ssl.SSLException;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {

    // @see https://stackoverflow.com/questions/45418523/spring-5-webclient-using-ssl/53147631#53147631

    @Bean
    @ConditionalOnProperty(name = "cf.sslValidationSkipped", havingValue="true")
    public WebClient insecureWebClient(WebClient.Builder builder) throws SSLException {
        SslContext context = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        HttpClient httpClient = HttpClient.create().secure(t -> t.sslContext(context));
        return WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient)).build();
    }

    @Bean
    @ConditionalOnProperty(name = "cf.sslValidationSkipped", havingValue="false", matchIfMissing=true)
    public WebClient secureWebClient(WebClient.Builder builder) {
        return builder.build();
    }
}
