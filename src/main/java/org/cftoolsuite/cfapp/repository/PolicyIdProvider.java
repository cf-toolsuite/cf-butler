package org.cftoolsuite.cfapp.repository;

import org.cftoolsuite.cfapp.client.GitClient;
import org.cftoolsuite.cfapp.config.GitSettings;
import org.cftoolsuite.cfapp.domain.ApplicationPolicy;
import org.cftoolsuite.cfapp.domain.EndpointPolicy;
import org.cftoolsuite.cfapp.domain.HygienePolicy;
import org.cftoolsuite.cfapp.domain.LegacyPolicy;
import org.cftoolsuite.cfapp.domain.QueryPolicy;
import org.cftoolsuite.cfapp.domain.ResourceNotificationPolicy;
import org.cftoolsuite.cfapp.domain.ServiceInstancePolicy;
import org.eclipse.jgit.lib.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PolicyIdProvider {

    private final GitSettings settings;
    private final String commit;

    public PolicyIdProvider(
            GitSettings settings,
            @Autowired(required = false) GitClient client
            ) {
        this.settings = settings;
        if (client != null && settings.isVersionManaged()) {
            Repository repo = client.getRepository(settings);
            this.commit =
                    settings.isPinnedCommit()
                    ? settings.getCommit()
                            : client.orLatestCommit(settings.getCommit(), repo);
        } else {
            this.commit = settings.getCommit();
        }
    }

    public ApplicationPolicy seedApplicationPolicy(ApplicationPolicy policy) {
        return settings.isVersionManaged() ? ApplicationPolicy.seedWith(policy, commit): ApplicationPolicy.seed(policy);
    }

    public EndpointPolicy seedEndpointPolicy(EndpointPolicy policy) {
        return settings.isVersionManaged() ? EndpointPolicy.seedWith(policy, commit): EndpointPolicy.seed(policy);
    }

    public HygienePolicy seedHygienePolicy(HygienePolicy policy) {
        return settings.isVersionManaged() ? HygienePolicy.seedWith(policy, commit): HygienePolicy.seed(policy);
    }

    public ResourceNotificationPolicy seedResourceNotificationPolicy(ResourceNotificationPolicy policy) {
        return settings.isVersionManaged() ? ResourceNotificationPolicy.seedWith(policy, commit): ResourceNotificationPolicy.seed(policy);
    }

    public LegacyPolicy seedLegacyPolicy(LegacyPolicy policy) {
        return settings.isVersionManaged() ? LegacyPolicy.seedWith(policy, commit): LegacyPolicy.seed(policy);
    }

    public QueryPolicy seedQueryPolicy(QueryPolicy policy) {
        return settings.isVersionManaged() ? QueryPolicy.seedWith(policy, commit): QueryPolicy.seed(policy);
    }

    public ServiceInstancePolicy seedServiceInstancePolicy(ServiceInstancePolicy policy) {
        return settings.isVersionManaged() ? ServiceInstancePolicy.seedWith(policy, commit): ServiceInstancePolicy.seed(policy);
    }

}
