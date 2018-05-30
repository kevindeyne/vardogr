package com.kevindeyne.datascrambler.domain;

public class ForeignKey {

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
