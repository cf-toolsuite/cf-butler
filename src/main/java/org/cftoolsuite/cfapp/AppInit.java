package org.cftoolsuite.cfapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import io.netty.util.ResourceLeakDetector;
import reactor.core.publisher.Hooks;


@EnableTransactionManagement
@ConfigurationPropertiesScan
@SpringBootApplication
public class AppInit {

    public static void main(String[] args) {
        Hooks.onOperatorDebug();
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED);
        SpringApplication.run(AppInit.class, args);
    }
}
