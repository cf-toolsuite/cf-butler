package org.cftoolsuite.cfapp.config;

import java.time.LocalDate;

import org.cftoolsuite.cfapp.deser.RelaxedLocalDateDeserializer;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tools.jackson.databind.module.SimpleModule;

@Configuration
public class JacksonDeSerConfig {

    @Bean
    public JsonMapperBuilderCustomizer addCustomDeserialization() {
        return builder -> {
            SimpleModule module = new SimpleModule();
            module.addDeserializer(LocalDate.class, new RelaxedLocalDateDeserializer());
            builder.addModule(module);
        };
    }
}
