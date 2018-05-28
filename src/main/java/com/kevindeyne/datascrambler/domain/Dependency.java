package com.kevindeyne.datascrambler.domain;

import java.util.ArrayList;
import java.util.List;

public class Dependency {

    String table;
    String column;
    Boolean handled = false;

    List<Dependency> children = new ArrayList<>();

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public List<Dependency> getChildren() {
        return children;
    }

    public Boolean getHandled() {
        return handled;
    }

    public void setHandled(Boolean handled) {
        this.handled = handled;
    }

    @Override
    public String toString() {
        return "{"+table+" depends on:"+children.size()+"}";
    }
}
