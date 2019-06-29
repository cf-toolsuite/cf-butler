package io.pivotal.cfapp.domain;

import java.util.Optional;

public class PoliciesValidator {

    public static boolean validate(ApplicationPolicy policy) {
        boolean hasId = Optional.ofNullable(policy.getId()).isPresent();
        boolean hasState = Optional.ofNullable(policy.getState()).isPresent();
        boolean hasFromDateTime = Optional.ofNullable(policy.getFromDateTime()).isPresent();
        boolean hasFromDuration = Optional.ofNullable(policy.getFromDuration()).isPresent();

        return !hasId && hasState &&
                    ((hasFromDateTime && !hasFromDuration) || (!hasFromDateTime && hasFromDuration));
    }

    public static boolean validate(ServiceInstancePolicy policy) {
        boolean hasId = Optional.ofNullable(policy.getId()).isPresent();
        boolean hasFromDateTime = Optional.ofNullable(policy.getFromDateTime()).isPresent();
        boolean hasFromDuration = Optional.ofNullable(policy.getFromDuration()).isPresent();

        return !hasId &&
                    ((hasFromDateTime && !hasFromDuration) || (!hasFromDateTime && hasFromDuration));
    }

}