package org.cftoolsuite.cfapp.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class OrganizationTest {

    @Test
    public void assertThatOrganizationsAreEqual() {
        Organization org1 = new Organization("000eaf", "zoo-labs");
        Organization org2 = new Organization("000eaf", "zoo-labs");
        assertTrue(org1.equals(org2));
        Set<Organization> orgs = new HashSet<>(List.of(org1, org2));
        assertEquals(orgs.size(), 1);
    }
}
