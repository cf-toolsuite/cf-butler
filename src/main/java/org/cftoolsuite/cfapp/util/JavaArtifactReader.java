package org.cftoolsuite.cfapp.util;

import java.util.Set;

public interface JavaArtifactReader {

    Set<String> read(String input);
    String mode();
}
