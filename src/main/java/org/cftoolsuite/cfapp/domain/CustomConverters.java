package org.cftoolsuite.cfapp.domain;

import java.util.List;

public class CustomConverters {

    public static List<Object> get() {
        return List.of(
            new AppDetailReadConverter(),
            new AppDetailWriteConverter(),
            new ApplicationPolicyReadConverter(),
            new ApplicationPolicyWriteConverter(),
            new EndpointPolicyReadConverter(),
            new EndpointPolicyWriteConverter(),
            new HygienePolicyReadConverter(),
            new HygienePolicyWriteConverter(),
            new LegacyPolicyReadConverter(),
            new LegacyPolicyWriteConverter(),
            new QueryPolicyReadConverter(),
            new QueryPolicyWriteConverter(),
            new ResourceNotificationPolicyReadConverter(),
            new ResourceNotificationPolicyWriteConverter(),
            new ServiceInstanceDetailReadConverter(),
            new ServiceInstanceDetailWriteConverter(),
            new ServiceInstancePolicyReadConverter(),
            new ServiceInstancePolicyWriteConverter(),
            new SpaceUsersReadConverter(),
            new SpaceUsersWriteConverter()
        );
    }
}
