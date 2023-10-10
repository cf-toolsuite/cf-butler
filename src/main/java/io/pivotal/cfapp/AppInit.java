package io.pivotal.cfapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import reactor.core.publisher.Hooks;


@EnableScheduling
@EnableTransactionManagement
@ConfigurationPropertiesScan
@SpringBootApplication
public class AppInit {

    public static void main(String[] args) {
        Hooks.onOperatorDebug();
        SpringApplication.run(AppInit.class, args);
    }
}
