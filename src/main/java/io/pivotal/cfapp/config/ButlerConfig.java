package io.pivotal.cfapp.config;

import java.time.Duration;

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
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableScheduling
@EnableTransactionManagement
public class ButlerConfig {

    @Bean
    public DefaultConnectionContext connectionContext(PasSettings settings) {
        return DefaultConnectionContext
                .builder()
                    .apiHost(settings.getApiHost())
                    .skipSslValidation(settings.isSslValidationSkipped())
                    .keepAlive(true)
                    .connectionPoolSize(settings.getConnectionPoolSize())
                    .connectTimeout(Duration.parse(settings.getConnectionTimeout()))
                    .build();
    }

    @Bean
    @ConditionalOnProperty(prefix="token", name="provider", havingValue="userpass", matchIfMissing=true)
    public TokenProvider tokenProvider(PasSettings settings) {
        return PasswordGrantTokenProvider
                .builder()
                    .username(settings.getUsername())
                    .password(settings.getPassword())
                    .build();
    }

    @Bean
    @ConditionalOnProperty(prefix="token", name="provider", havingValue="sso")
    public TokenProvider refreshGrantTokenProvider(PasSettings settings) {
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

    

}
