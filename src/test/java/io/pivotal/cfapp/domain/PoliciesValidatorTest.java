package io.pivotal.cfapp.domain;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class PoliciesValidatorTest {

    @Test
    public void assertValidApplicationPolicy() {
        Map<String, Object> options = new HashMap<>();
        options.put("from-duration", "PT30S");
        ApplicationPolicy durationPolicy =
            ApplicationPolicy
                .builder()
                .description("Delete applications in stopped state that were pushed more than 30s ago.")
                .operation(ApplicationOperation.DELETE.getName())
                .organizationWhiteList(Set.of("zoo-labs"))
                .options(options)
                .state(ApplicationState.STOPPED.getName())
                .pk(100L)
                .id(null)
                .build();
        Assertions.assertThat(PoliciesValidator.validate(durationPolicy) == true);

        ApplicationPolicy noTimeframePolicy =
            ApplicationPolicy
                .builder()
                .description("Delete all applications in stopped state.")
                .operation(ApplicationOperation.DELETE.getName())
                .organizationWhiteList(Set.of("zoo-labs"))
                .state(ApplicationState.STOPPED.getName())
                .pk(100L)
                .id(null)
                .build();
        Assertions.assertThat(PoliciesValidator.validate(noTimeframePolicy) == true);

        Map<String, Object> options2 = new HashMap<>();
        options.put("from-datetime", LocalDateTime.now().minusDays(2));
        ApplicationPolicy dateTimePolicy =
            ApplicationPolicy
                .builder()
                .description("Delete all applications in stopped state that were pushed after date/time.")
                .operation(ApplicationOperation.DELETE.getName())
                .options(options2)
                .organizationWhiteList(Set.of("zoo-labs"))
                .state(ApplicationState.STOPPED.getName())
                .pk(100L)
                .id(null)
                .build();
        Assertions.assertThat(PoliciesValidator.validate(dateTimePolicy) == true);
    }

    @Test
    public void assertInvalidApplicationPolicy() {
        Map<String, Object> options = new HashMap<>();
        options.put("from-duration", "PT30S");
        options.put("from-datetime", LocalDateTime.now().minusSeconds(30));
        ApplicationPolicy invalidPolicy =
            ApplicationPolicy
                .builder()
                .description("Delete applications in stiopped state that were pushed more than 30s ago.")
                .operation(ApplicationOperation.DELETE.getName())
                .organizationWhiteList(Set.of("zoo-labs"))
                .options(options)
                .state(ApplicationState.STOPPED.getName())
                .pk(100L)
                .id(null)
                .build();
        Assertions.assertThat(PoliciesValidator.validate(invalidPolicy) == false);
    }
}