package org.cftoolsuite.cfapp.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class JarManifestUtil {

    public static String obtainAttributeValue(String contents, String key) throws IOException {
        Manifest manifest = new Manifest(new ByteArrayInputStream(contents.getBytes()));
        Attributes attributes = manifest.getMainAttributes();
        return attributes.getValue(key);
    }

}
