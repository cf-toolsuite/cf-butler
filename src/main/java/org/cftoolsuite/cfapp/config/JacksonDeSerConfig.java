package org.cftoolsuite.cfapp.config;

import java.time.LocalDate;

import org.cftoolsuite.cfapp.deser.RelaxedLocalDateDeserializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.module.SimpleModule;

@Configuration
public class JacksonDeSerConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer addCustomDeserialization() {
        return builder -> {
            SimpleModule module = new SimpleModule();
            module.addDeserializer(LocalDate.class, new RelaxedLocalDateDeserializer());
            builder.modulesToInstall(module);
        };
    }
}
