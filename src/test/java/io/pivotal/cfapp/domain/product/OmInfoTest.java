package io.pivotal.cfapp.domain.product;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class OmInfoTest {

    @Test
    public void testGetMajorMinorVersion() {
        OmInfo info = OmInfo.builder().info(new OmInfo.Info("2.5.0-build.79")).build();
        Double expected = 2.5;
        Double actual = info.getMajorMinorVersion();
        Assertions.assertEquals(actual, expected);
    }

    @Test
    public void testGetMajorMinorVersionWithBuildButNoDot() {
        OmInfo info = OmInfo.builder().info(new OmInfo.Info("2.5.0-build")).build();
        Double expected = 2.5;
        Double actual = info.getMajorMinorVersion();
        Assertions.assertEquals(actual, expected);
    }

    @Test
    public void testGetMajorMinorVersionNoBuild() {
        OmInfo info = OmInfo.builder().info(new OmInfo.Info("2.5.0")).build();
        Double expected = 2.5;
        Double actual = info.getMajorMinorVersion();
        Assertions.assertEquals(actual, expected);
    }

    @Test
    public void testGetMajorMinorVersionNoReleaseNoBuild() {
        OmInfo info = OmInfo.builder().info(new OmInfo.Info("2.5")).build();
        Double expected = 2.5;
        Double actual = info.getMajorMinorVersion();
        Assertions.assertEquals(actual, expected);
    }

}
