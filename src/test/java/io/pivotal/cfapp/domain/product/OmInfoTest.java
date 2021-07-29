package io.pivotal.cfapp.domain.product;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class OmInfoTest {

    @Test
    public void testGetMajorMinorVersion() {
        OmInfo info = OmInfo.builder().info(new OmInfo.Info("2.5.0-build.79")).build();
        Integer expectedMajor = 2;
        Integer expectedMinor = 5;
        Integer actualMajor = info.getMajorVersion();
        Integer actualMinor = info.getMinorVersion();
        Assertions.assertEquals(actualMajor, expectedMajor);
        Assertions.assertEquals(actualMinor, expectedMinor);
    }

    @Test
    public void testGetMajorMinorVersionWithBuildButNoDot() {
        OmInfo info = OmInfo.builder().info(new OmInfo.Info("2.5.0-build")).build();
        Integer expectedMajor = 2;
        Integer expectedMinor = 5;
        Integer actualMajor = info.getMajorVersion();
        Integer actualMinor = info.getMinorVersion();
        Assertions.assertEquals(actualMajor, expectedMajor);
        Assertions.assertEquals(actualMinor, expectedMinor);
    }

    @Test
    public void testGetMajorMinorVersionNoBuild() {
        OmInfo info = OmInfo.builder().info(new OmInfo.Info("2.5.0")).build();
        Integer expectedMajor = 2;
        Integer expectedMinor = 5;
        Integer actualMajor = info.getMajorVersion();
        Integer actualMinor = info.getMinorVersion();
        Assertions.assertEquals(actualMajor, expectedMajor);
        Assertions.assertEquals(actualMinor, expectedMinor);
    }

    @Test
    public void testGetMajorMinorVersionNoReleaseNoBuild() {
        OmInfo info = OmInfo.builder().info(new OmInfo.Info("2.5")).build();
        Integer expectedMajor = 2;
        Integer expectedMinor = 5;
        Integer actualMajor = info.getMajorVersion();
        Integer actualMinor = info.getMinorVersion();
        Assertions.assertEquals(actualMajor, expectedMajor);
        Assertions.assertEquals(actualMinor, expectedMinor);
    }

}
