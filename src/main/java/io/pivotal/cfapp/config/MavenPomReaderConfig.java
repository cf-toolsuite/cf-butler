package io.pivotal.cfapp.config;

import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.pivotal.cfapp.util.MavenPomReader;

@Configuration
public class MavenPomReaderConfig {

    @Bean
    public MavenPomReader reader() {
        return new MavenPomReader(Set.of("org.springframework", "io.pivotal.spring.cloud"), true);
    }

}
