package com.kevindeyne.datascrambler.domain;

public class ForeignKey {

    public static final String FKTABLE_NAME = "FKTABLE_NAME";
    public static final String FKCOLUMN_NAME = "FKCOLUMN_NAME";
    public static final String PKTABLE_NAME = "PKTABLE_NAME";
    public static final String PKCOLUMN_NAME = "PKCOLUMN_NAME";
    private Table table;
    private String via;

    public ForeignKey(Table table, String columnName) {
        this.table = table;
        this.via = columnName;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public String getVia() {
        return via;
    }

    public void setVia(String columnName) {
        this.via = columnName;
    }
}
