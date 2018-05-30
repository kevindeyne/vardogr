package com.kevindeyne.datascrambler.domain;

public class ForeignKey {

    private Table table;
    private String via;
    private Long fkCount;
    private Long tableSize;

    public ForeignKey(Table table, String columnName, Long count, Long tableSize) {
        this.table = table;
        this.via = columnName;
        this.fkCount = count;
        this.tableSize = tableSize;
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

    public Long getFkCount() {
        return fkCount;
    }

    public void setFkCount(Long count) {
        this.fkCount = count;
    }

    public Long getTableSize() {
        return tableSize;
    }

    public void setTableSize(Long tableSize) {
        this.tableSize = tableSize;
    }
}
