package org.cftoolsuite.cfapp.domain;

public interface HasCronExpression {
    String getCronExpression();

    default String defaultCronExpression() {
        return "0 0 2 * * MON";
    }
}
