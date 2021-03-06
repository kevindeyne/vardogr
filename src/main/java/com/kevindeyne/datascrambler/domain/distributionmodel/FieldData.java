package com.kevindeyne.datascrambler.domain.distributionmodel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

@Data
public class FieldData {

    @SerializedName(value = "n")
    private String fieldName;
    @SerializedName(value = "g")
    private Generator generator;
    @SerializedName(value = "v")
    private ValueDistribution valueDistribution;
    @SerializedName(value = "p")
    private boolean primaryKey;
    @SerializedName(value = "f")
    private ForeignKeyData foreignKeyData;
    @SerializedName(value = "c")
    private List<String> characteristics;
    @Expose(serialize = false)
    private int offset = 0;

    public FieldData(String name) {
        this.fieldName = name;
    }

}
