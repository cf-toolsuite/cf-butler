package io.pivotal.cfapp.util;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import io.pivotal.cfapp.config.PasSettings;
import io.pivotal.cfapp.domain.HasOrganizationWhiteList;

@Component
public class PolicyFilter {
    
    private PasSettings settings;

    @Autowired
    public PolicyFilter(PasSettings settings) {
        this.settings = settings;
    }

    public boolean isBlacklisted(String organization, String space) {
        if (settings.hasSpaceBlackList()) {
            return !settings.getSpaceBlackList().contains(String.join(":", organization, space));
        } else {
            return !settings.getOrganizationBlackList().contains(organization);
        }
    }

    public boolean isWhitelisted(HasOrganizationWhiteList policy, String organization) {
        Set<String> prunedSet = new HashSet<>(policy.getOrganizationWhiteList());
        while (prunedSet.remove(""));
        Set<String> whitelist =
                CollectionUtils.isEmpty(prunedSet) ?
                        prunedSet: policy.getOrganizationWhiteList();
        return
                whitelist.isEmpty() ? true: policy.getOrganizationWhiteList().contains(organization);
    }
}
