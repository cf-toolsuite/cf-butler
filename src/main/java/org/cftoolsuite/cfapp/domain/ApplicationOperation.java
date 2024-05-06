package org.cftoolsuite.cfapp.domain;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.cftoolsuite.cfapp.task.DeleteAppPolicyExecutorTask;
import org.cftoolsuite.cfapp.task.PolicyExecutorTask;
import org.cftoolsuite.cfapp.task.ScaleAppInstancesPolicyExecutorTask;
import org.cftoolsuite.cfapp.task.StackChangeAppInstancesPolicyExecutorTask;
import org.cftoolsuite.cfapp.task.StopAppPolicyExecutorTask;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ApplicationOperation {

    DELETE("delete"),
    SCALE_INSTANCES("scale-instances"),
    STOP("stop"),
    CHANGE_STACK("change-stack");

    private final String name;

    ApplicationOperation(String name) {
        this.name = name;
    }

    static final Map<ApplicationOperation, Class<? extends PolicyExecutorTask>> operationTaskMap = new EnumMap<>(ApplicationOperation.class);
    static {
        operationTaskMap.put(ApplicationOperation.DELETE, DeleteAppPolicyExecutorTask.class);
        operationTaskMap.put(ApplicationOperation.SCALE_INSTANCES, ScaleAppInstancesPolicyExecutorTask.class);
        operationTaskMap.put(ApplicationOperation.STOP, StopAppPolicyExecutorTask.class);
        operationTaskMap.put(ApplicationOperation.CHANGE_STACK, StackChangeAppInstancesPolicyExecutorTask.class);
    }

    public static ApplicationOperation from(String name) {
        Assert.hasText(name, "ApplicationOperation must not be null or empty");
        ApplicationOperation result = Arrays.asList(ApplicationOperation.values()).stream().filter(s -> s.getName().equalsIgnoreCase(name)).collect(Collectors.toList()).get(0);
        Assert.notNull(result, String.format("Invalid ApplicationOperation, name=%s", name));
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
