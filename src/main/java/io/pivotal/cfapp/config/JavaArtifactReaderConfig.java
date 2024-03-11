package io.pivotal.cfapp.config;

import java.util.Map;
import java.util.Set;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import io.pivotal.cfapp.util.JarSetFilterReader;
import io.pivotal.cfapp.util.JarSetFilterReaderCondition;
import io.pivotal.cfapp.util.JavaArtifactReader;
import io.pivotal.cfapp.util.MavenPomReader;

@Configuration
public class JavaArtifactReaderConfig {

    @Configuration
    @ConditionalOnProperty(prefix = "java.artifacts.fetch", name= "mode", havingValue="unpack-pom-contents-in-droplet")
    static class MavenPomReaderConfig {

        @Bean
        public JavaArtifactReader mavenPomReader() {
            return new MavenPomReader(
                Set.of(
                    "org.springframework",
                    "io.pivotal.spring.cloud"
                ),
                true
            );
        }
    }

    @Configuration
    @Conditional(JarSetFilterReaderCondition.class)
    static class JarSetFilterReaderConfig {

        @Bean
        public JavaArtifactReader jarSetFilterReader() {
            return new JarSetFilterReader(
                Map.of(
                    "spring-core", "org.springframework",
                    "spring-boot", "org.springframework.boot",
                    "spring-cloud-context", "org.springframework.cloud",
                    "spring-data-commons", "org.springframework.data"
                )
            );
        }
    }

}
