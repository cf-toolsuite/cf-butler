package io.pivotal.cfapp.domain;

public enum IndexPrefix {
    H2("$"),
    POSTGRES("$"),
    MSSQL("@");

    private final String symbol;

    IndexPrefix(String symbol){
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

}