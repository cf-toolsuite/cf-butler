package io.pivotal.cfapp.domain;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

public class Buildpack {

    private static String[] packs =
        {   "staticfile", "java", "ruby", "nodejs",
            "go", "python", "php", "dotnet",
            "hwc", "binary", "tomcat", "liberty",
            "jboss", "apt", "nginx", "clojure", "haskell",
            "tomee", "jetty", "meteor", "erlang", "elixir",
            "swift", "rust", "emberjs", "pyspark", "tc",
            "weblogic" };

    public static String is(String input) {
        if (StringUtils.isBlank(input)) {
            return "unknown";
        }
        Set<String> candidate =
                Arrays.asList(packs)
                        .stream()
                            .filter(p -> input.contains(p))
                            .collect(Collectors.toSet());
        if (candidate.size() == 1) {
            return candidate.iterator().next();
        } else {
            return "unknown";
        }
    }

    public static List<String> list() {
        return Arrays.asList(packs);
    }
}
