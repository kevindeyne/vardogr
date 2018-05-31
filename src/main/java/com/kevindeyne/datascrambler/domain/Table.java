package com.kevindeyne.datascrambler.domain;

import java.util.ArrayList;
import java.util.List;

public class Table {

    private String name;
    private List<String> pks;
    private List<ForeignKey> fks;
    private List<Column> columns;
    private Long tableSize;

    public Table(String name) {
        this.name = name;
        pks = new ArrayList<>();
        fks = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getPks() {
        return pks;
    }

    public void setPks(List<String> pks) {
        this.pks = pks;
    }

    public List<ForeignKey> getFks() {
        return fks;
    }

    public void setFks(List<ForeignKey> fks) {
        this.fks = fks;
    }

    public Long getTableSize() {
        return tableSize;
    }

    public void setTableSize(Long tableSize) {
        this.tableSize = tableSize;
    }

    public List<Column> getColumns() {
        return columns;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Table) {
            Table other = (Table)obj;
            return other.getName().equals(getName());
        } else {
            return false;
        }
    }

    public void resetColumns() {
        columns = new ArrayList<>();
    }
}
