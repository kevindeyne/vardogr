package com.kevindeyne.datascrambler.domain.distributionmodel;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TableData {

    @SerializedName(value = "n")
    private String tableName;

    @SerializedName(value = "t")
    private long totalCount;

    @SerializedName(value = "f")
    private List<FieldData> fieldData = new ArrayList<>();

    public TableData(String name) {
        this.tableName = name;
    }

}
