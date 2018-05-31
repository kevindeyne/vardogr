package com.kevindeyne.datascrambler.domain;

public class Column {

    private String name;
    private Class clazz;
    private int precision;
    private boolean allowsNulls;

    public Column(String name, Class clazz, int precision, boolean allowsNulls) throws ClassNotFoundException {
        this.clazz = clazz;
        this.name = name;
        this.precision = precision;
        this.allowsNulls = allowsNulls;
    }

    public String getName() {
        return name;
    }

    public Class getClazz() {
        return clazz;
    }

    public int getPrecision() {
        return precision;
    }

    public boolean isNullable() {
        return allowsNulls;
    }
}
