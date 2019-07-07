package io.pivotal.cfapp.config;

import java.time.Duration;

import javax.net.ssl.SSLException;

import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.reactor.ConnectionContext;
import org.cloudfoundry.reactor.DefaultConnectionContext;
import org.cloudfoundry.reactor.TokenProvider;
import org.cloudfoundry.reactor.client.ReactorCloudFoundryClient;
import org.cloudfoundry.reactor.doppler.ReactorDopplerClient;
import org.cloudfoundry.reactor.tokenprovider.PasswordGrantTokenProvider;
import org.cloudfoundry.reactor.tokenprovider.RefreshTokenGrantTokenProvider;
import org.cloudfoundry.reactor.uaa.ReactorUaaClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

@Configuration
@EnableScheduling
@EnableTransactionManagement
public class ButlerConfig {

    @Bean
    public DefaultConnectionContext connectionContext(ButlerSettings settings) {
        return DefaultConnectionContext
                .builder()
                    .apiHost(settings.getApiHost())
                    .skipSslValidation(settings.isSslValidationSkipped())
                    .connectionPoolSize(settings.getConnectionPoolSize())
                    .connectTimeout(Duration.ofMinutes(3))
                    .build();
    }

    @Bean
    @ConditionalOnProperty(prefix="token", name="provider", havingValue="userpass", matchIfMissing=true)
    public TokenProvider tokenProvider(ButlerSettings settings) {
        return PasswordGrantTokenProvider
                .builder()
                    .username(settings.getUsername())
                    .password(settings.getPassword())
                    .build();
    }

    @Bean
    @ConditionalOnProperty(prefix="token", name="provider", havingValue="sso")
    public TokenProvider refreshGrantTokenProvider(ButlerSettings settings) {
        return RefreshTokenGrantTokenProvider
                .builder()
                    .token(settings.getRefreshToken())
                    .build();
    }

    @Bean
    public ReactorCloudFoundryClient cloudFoundryClient(ConnectionContext connectionContext, TokenProvider tokenProvider) {
        return ReactorCloudFoundryClient
                .builder()
                    .connectionContext(connectionContext)
                    .tokenProvider(tokenProvider)
                    .build();
}

    @Bean
    public ReactorDopplerClient dopplerClient(ConnectionContext connectionContext, TokenProvider tokenProvider) {
        return ReactorDopplerClient
                .builder()
                    .connectionContext(connectionContext)
                    .tokenProvider(tokenProvider)
                    .build();
    }

    @Bean
    public ReactorUaaClient uaaClient(ConnectionContext connectionContext, TokenProvider tokenProvider) {
        return ReactorUaaClient
                .builder()
                    .connectionContext(connectionContext)
                    .tokenProvider(tokenProvider)
                    .build();
    }

    @Bean
    public DefaultCloudFoundryOperations opsClient(ReactorCloudFoundryClient cloudFoundryClient, 
            ReactorDopplerClient dopplerClient, ReactorUaaClient uaaClient) {
        return DefaultCloudFoundryOperations
                .builder()
                    .cloudFoundryClient(cloudFoundryClient)
                    .dopplerClient(dopplerClient)
                    .uaaClient(uaaClient)
                    .build();
    }

    @Bean(name = "applicationEventMulticaster")
    public ApplicationEventMulticaster simpleApplicationEventMulticaster() {
        SimpleApplicationEventMulticaster eventMulticaster =
            new SimpleApplicationEventMulticaster();
        eventMulticaster.setTaskExecutor(new SimpleAsyncTaskExecutor());
        return eventMulticaster;
    }

    // @see https://stackoverflow.com/questions/45418523/spring-5-webclient-using-ssl/53147631#53147631

    @Bean
    @ConditionalOnProperty(prefix="cf", name="sslValidationSkipped", havingValue="true")
    public WebClient insecureWebClient() throws SSLException {
        SslContext sslContext =
            SslContextBuilder
                .forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();
        TcpClient tcpClient = TcpClient.create().secure(sslProviderBuilder -> sslProviderBuilder.sslContext(sslContext));
        HttpClient httpClient = HttpClient.from(tcpClient);
        return WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient)).build();
    }

    @Bean
    @ConditionalOnProperty(prefix="cf", name="sslValidationSkipped", havingValue="false", matchIfMissing=true)
    public WebClient secureWebClient() throws SSLException {
        return WebClient.builder().build();
    }

}
