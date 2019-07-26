package io.pivotal.cfapp.repository;

import org.eclipse.jgit.lib.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.client.GitClient;
import io.pivotal.cfapp.config.PoliciesSettings;
import io.pivotal.cfapp.domain.ApplicationPolicy;
import io.pivotal.cfapp.domain.QueryPolicy;
import io.pivotal.cfapp.domain.ServiceInstancePolicy;

@Component
public class PolicyIdProvider {

    private final PoliciesSettings settings;
    private final String commit;

    @Autowired(required = false)
    public PolicyIdProvider(
        PoliciesSettings settings,
        GitClient client
    ) {
        this.settings = settings;
        if (client != null) {
            Repository repo = client.getRepository(settings.getUri());
            this.commit =
                settings.getCommit() != null
                    ? settings.getCommit()
                    : client.orLatestCommit(settings.getCommit(), repo);
        } else {
            this.commit = settings.getCommit();
        }
    }

    public ApplicationPolicy seedApplicationPolicy(ApplicationPolicy policy) {
		return settings.isVersionManaged() ? ApplicationPolicy.seedWith(policy, commit): ApplicationPolicy.seed(policy);
	}

	public ServiceInstancePolicy seedServiceInstancePolicy(ServiceInstancePolicy policy) {
		return settings.isVersionManaged() ? ServiceInstancePolicy.seedWith(policy, commit): ServiceInstancePolicy.seed(policy);
	}

	public QueryPolicy seedQueryPolicy(QueryPolicy policy) {
		return settings.isVersionManaged() ? QueryPolicy.seedWith(policy, commit): QueryPolicy.seed(policy);
    }

}