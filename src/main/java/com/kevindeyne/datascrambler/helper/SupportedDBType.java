package com.kevindeyne.datascrambler.helper;

import java.util.ArrayList;
import java.util.List;

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

    public static List<String> all() {
        List<String> all = new ArrayList<>();
        for(SupportedDBType value : values()) all.add(value.name());
        return all;
    }

    public String getPlaceholder() {
        return placeholder;
    }
}
