package io.pivotal.cfapp.util;

import java.util.Set;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;


public class DropletProcessingCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Set<String> activationValues = Set.of("unpack-pom-contents-in-droplet", "list-jars-in-droplet");
        Environment env = context.getEnvironment();
        return activationValues.contains(env.getProperty("java.artifacts.fetch.mode",""));
    }
}