package org.cftoolsuite.cfapp.domain;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.cftoolsuite.cfapp.task.DeleteServiceInstancePolicyExecutorTask;
import org.cftoolsuite.cfapp.task.PolicyExecutorTask;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ServiceInstanceOperation {

    DELETE("delete");

    private final String name;

    ServiceInstanceOperation(String name) {
        this.name = name;
    }

    static final Map<ServiceInstanceOperation, Class<? extends PolicyExecutorTask>> operationTaskMap = new EnumMap<>(ServiceInstanceOperation.class);
    static {
        operationTaskMap.put(ServiceInstanceOperation.DELETE, DeleteServiceInstancePolicyExecutorTask.class);
    }

    public static ServiceInstanceOperation from(String name) {
        Assert.hasText(name, "ServiceInstanceOperation must not be null or empty");
        ServiceInstanceOperation result = Arrays.asList(ServiceInstanceOperation.values()).stream().filter(s -> s.getName().equalsIgnoreCase(name)).collect(Collectors.toList()).get(0);
        Assert.notNull(result, String.format("Invalid ServiceInstanceOperation, name=%s", name));
        return result;
    }

    public static Class<? extends PolicyExecutorTask> getTaskType(String op) {
        return operationTaskMap.get(from(op));
    }

    @JsonValue
    public String getName() {
        return name;
    }
}
