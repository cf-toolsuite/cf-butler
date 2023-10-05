package io.pivotal.cfapp.domain;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class OrganizationTest {

    @Test
    public void assertThatOrganizationsAreEqual() {
        Organization org1 = new Organization("000eaf", "zoo-labs");
        Organization org2 = new Organization("000eaf", "zoo-labs");
        Assertions.assertTrue(org1.equals(org2));
        Set<Organization> orgs = new HashSet<>(List.of(org1, org2));
        Assertions.assertTrue(orgs.size() == 1);
    }
}
