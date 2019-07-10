package io.pivotal.cfapp.domain;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.service.StacksCache;
@Component
public class PoliciesValidator {

    private final StacksCache stacksCache;

    @Autowired
    public PoliciesValidator(StacksCache stacksCache) {
        this.stacksCache = stacksCache;
    }

    public boolean validate(ApplicationPolicy policy) {
        boolean hasId = Optional.ofNullable(policy.getId()).isPresent();
        boolean hasOperation = Optional.ofNullable(policy.getOperation()).isPresent();
        boolean hasState = Optional.ofNullable(policy.getState()).isPresent();
        boolean hasFromDateTime = Optional.ofNullable(policy.getOption("from-datetime", LocalDateTime.class)).isPresent();
        boolean hasFromDuration = Optional.ofNullable(policy.getOption("from-duration", String.class)).isPresent();
        boolean valid = !hasId && hasOperation && hasState;
        if (hasOperation) {
            try {
                ApplicationOperation op = ApplicationOperation.from(policy.getOperation());
                if (op.equals(ApplicationOperation.SCALE_INSTANCES)) {
                    Integer instancesFrom = policy.getOption("instances-from", Integer.class);
                    Integer instancesTo = policy.getOption("instances-to", Integer.class);
                    if (instancesFrom == null || instancesTo == null || instancesFrom < 1 || instancesTo < 1 || instancesFrom == instancesTo) {
                        valid = false;
                    }
                }
                if (op.equals(ApplicationOperation.CHANGE_STACK)) {
                    String stackFrom = policy.getOption("stack-from", String.class);
                    String stackTo = policy.getOption("stack-to", String.class);
                    if (!stacksCache.contains(stackFrom) || !stacksCache.contains(stackTo) || stackFrom.equalsIgnoreCase(stackTo)) {
                        valid = false;
                    }
                }
            } catch (IllegalArgumentException iae) {
                valid = false;
            }
        }
        if (hasState) {
            try {
                ApplicationState.from(policy.getState());
            } catch (IllegalArgumentException iae) {
                valid = false;
            }
        }
        if (hasFromDateTime && hasFromDuration) {
            valid = false;
        }
        if (hasFromDuration) {
            try {
                Duration.parse(policy.getOption("from-duration", String.class));
            } catch (DateTimeParseException dtpe) {
                valid = false;
            }
        }
        return valid;
    }

    public boolean validate(ServiceInstancePolicy policy) {
        boolean hasId = Optional.ofNullable(policy.getId()).isPresent();
        boolean hasOperation = Optional.ofNullable(policy.getOperation()).isPresent();
        boolean hasFromDateTime = Optional.ofNullable(policy.getOption("from-datetime", LocalDateTime.class)).isPresent();
        boolean hasFromDuration = Optional.ofNullable(policy.getOption("from-duration", String.class)).isPresent();
        boolean valid = !hasId && hasOperation;
        if (hasOperation) {
            try {
                ServiceInstanceOperation.from(policy.getOperation());
            } catch (IllegalArgumentException iae) {
                valid = false;
            }
        }
        if (hasFromDateTime && hasFromDuration) {
            valid = false;
        }
        if (hasFromDuration) {
            try {
                Duration.parse(policy.getOption("from-duration", String.class));
            } catch (DateTimeParseException dtpe) {
                valid = false;
            }
        }
        return valid;
    }

}