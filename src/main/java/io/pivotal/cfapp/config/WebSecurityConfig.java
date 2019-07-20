package io.pivotal.cfapp.config;

import org.springframework.boot.autoconfigure.security.reactive.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.SecurityWebFilterChain;

@EnableWebFluxSecurity
public class WebSecurityConfig {

    private static final String[] PREAUTHORIZED_PATHS =
        new String[] {
            "/", "/accounting/**", "/actuator/**", "/collect/**", "/policies/**",
            "/snapshot/**", "/store/**"
        };

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
            .authorizeExchange()
                .matchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
				.pathMatchers(PREAUTHORIZED_PATHS).permitAll()
				    .anyExchange().authenticated()
                .and()
            .oauth2Login()
                .and()
			.oauth2Client();
		return http.build();
    }

    @Bean
    public MapReactiveUserDetailsService userDetailsService(OpsmanSettings opsmanSettings) {
        UserDetails user =
            User
                .withUsername(opsmanSettings.getUsername())
                .password(opsmanSettings.getPassword())
                .roles("USER")
                .build();
        return new MapReactiveUserDetailsService(user);
    }
}