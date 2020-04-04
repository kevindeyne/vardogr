package com.kevindeyne.datascrambler.domain.distributionmodel;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TableData {

    private String tableName;
    private long totalCount;
    private List<FieldData> fieldData = new ArrayList<>();

    public TableData(String name) {
        this.tableName = name;
    }

}
