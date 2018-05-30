package com.kevindeyne.datascrambler.domain;

public class ForeignKey {

    private Table table;
    private String via;
    private Long count;

    public ForeignKey(Table table, String columnName, Long count) {
        this.table = table;
        this.via = columnName;
        this.count = count;
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

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
