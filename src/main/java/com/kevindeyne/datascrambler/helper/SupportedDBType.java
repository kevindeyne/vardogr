package com.kevindeyne.datascrambler.helper;

public enum SupportedDBType {

    ORACLE("jdbc:oracle:thin:@host:port:dbname"),
    MYSQL("jdbc:mysql://host:port/dbname"),
    POSTGRESQL("jdbc:postgresql://host:port/dbname"),
    SQL_SERVER("jdbc:sqlserver://host:port;instance=SQLEXPRESS;databaseName=dbname"),
    MARIADB("jdbc:mariadb://host:port/dbname");

    private String placeholder;

    SupportedDBType(String placeholder) {
        this.placeholder = placeholder;
    }

    public String getPlaceholder() {
        return placeholder;
    }
}
