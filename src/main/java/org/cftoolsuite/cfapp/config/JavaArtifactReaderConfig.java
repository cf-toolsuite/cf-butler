package org.cftoolsuite.cfapp.config;

import java.util.Map;
import java.util.Set;

import org.cftoolsuite.cfapp.util.JarSetFilterReader;
import org.cftoolsuite.cfapp.util.JarSetFilterReaderCondition;
import org.cftoolsuite.cfapp.util.JavaArtifactReader;
import org.cftoolsuite.cfapp.util.MavenPomReader;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

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
