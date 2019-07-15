package io.pivotal.cfapp.domain;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.lang.Collections;
import io.pivotal.cfapp.service.StacksCache;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Component
public class PoliciesValidator {

    private static final String REQUIRED_PROPERTIES_REJECTED_MESSAGE = "-- {} was rejected because required properties failed validation.";
    private static final String SCALE_INSTANCES_REJECTED_MESSAGE = "-- {} was rejected because instances-from and/or instances-to in options failed validation.";
    private static final String CHANGE_STACK_REJECTED_MESSAGE = "-- {} was rejected because stack-from and/or stack-to in options failed validation.";
    private static final String PARSING_REJECTED_MESSAGE = "-- {} was rejected because one or more of its properties could be parsed succesfully. {}";
    private static final String DUAL_TIME_CONSTRAINTS_REJECTED_MESSAGE = "-- {} was rejected because it contained both from-datetime and from-duration in options. Choose only one time constraint.";
    private static final String QUERY_REJECTED_MESSAGE = "-- {} was rejected because either name or sql was blank or sql did not start with SELECT.";
    private static final String EMAIL_NOTIFICATION_TEMPLATE_REJECTED_MESSAGE = "-- {} was rejected because either the email template did not contain valid email addresses for from/to or the subject/body was blank.";

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
                        log.warn(SCALE_INSTANCES_REJECTED_MESSAGE, policy.toString());
                    }
                }
                if (op.equals(ApplicationOperation.CHANGE_STACK)) {
                    String stackFrom = policy.getOption("stack-from", String.class);
                    String stackTo = policy.getOption("stack-to", String.class);
                    if (!stacksCache.contains(stackFrom) || !stacksCache.contains(stackTo) || stackFrom.equalsIgnoreCase(stackTo)) {
                        valid = false;
                        log.warn(CHANGE_STACK_REJECTED_MESSAGE, policy.toString());
                    }
                }
            } catch (IllegalArgumentException iae) {
                valid = false;
                log.warn(PARSING_REJECTED_MESSAGE, policy.toString(), iae.getMessage());
            }
        }
        if (hasState) {
            try {
                ApplicationState.from(policy.getState());
            } catch (IllegalArgumentException iae) {
                valid = false;
                log.warn(PARSING_REJECTED_MESSAGE, policy.toString(), iae.getMessage());
            }
        }
        if (hasFromDateTime && hasFromDuration) {
            valid = false;
            log.warn(DUAL_TIME_CONSTRAINTS_REJECTED_MESSAGE, policy.toString());
        }
        if (hasFromDuration) {
            try {
                Duration.parse(policy.getOption("from-duration", String.class));
            } catch (DateTimeParseException dtpe) {
                valid = false;
                log.warn(PARSING_REJECTED_MESSAGE, policy.toString(), dtpe.getMessage());
            }
        }
        if (valid == false) {
            log.warn(REQUIRED_PROPERTIES_REJECTED_MESSAGE, policy.toString());
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
                log.warn(PARSING_REJECTED_MESSAGE, policy.toString(), iae.getMessage());
            }
        }
        if (hasFromDateTime && hasFromDuration) {
            valid = false;
            log.warn(DUAL_TIME_CONSTRAINTS_REJECTED_MESSAGE, policy.toString());
        }
        if (hasFromDuration) {
            try {
                Duration.parse(policy.getOption("from-duration", String.class));
            } catch (DateTimeParseException dtpe) {
                valid = false;
                log.warn(PARSING_REJECTED_MESSAGE, policy.toString(), dtpe.getMessage());
            }
        }
        if (valid == false) {
            log.warn(REQUIRED_PROPERTIES_REJECTED_MESSAGE, policy.toString());
        }
        return valid;
    }

    public boolean validate(QueryPolicy policy) {
        boolean hasId = Optional.ofNullable(policy.getId()).isPresent();
        boolean hasQueries = Optional.ofNullable(policy.getQueries()).isPresent();
        boolean hasEmailNotificationTemplate = Optional.ofNullable(policy.getEmailNotificationTemplate()).isPresent();
        boolean valid = !hasId && hasQueries && hasEmailNotificationTemplate;
        if (hasQueries) {
            if (Collections.isEmpty(policy.getQueries())) {
                valid = false;
            } else {
                for (Query q: policy.getQueries()) {
                    if (!q.isValid()) {
                        valid = false;
                        log.warn(QUERY_REJECTED_MESSAGE, policy.toString());
                        break;
                    }
                }
            }
        }
        if (hasEmailNotificationTemplate) {
            if (!policy.getEmailNotificationTemplate().isValid()) {
                valid = false;
                log.warn(EMAIL_NOTIFICATION_TEMPLATE_REJECTED_MESSAGE, policy.toString());
            }
        }
        if (valid == false) {
            log.warn(REQUIRED_PROPERTIES_REJECTED_MESSAGE, policy.toString());
        }
        return valid;
    }

}