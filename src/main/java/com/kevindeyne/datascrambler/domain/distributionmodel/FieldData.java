package com.kevindeyne.datascrambler.domain.distributionmodel;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import org.jooq.impl.SQLDataType;

@Data
public class FieldData {

    @SerializedName(value = "n")
    private String fieldName;
    @SerializedName(value = "g")
    private Generator generator;
    @SerializedName(value = "v")
    private ValueDistribution valueDistribution;

    public FieldData(String name) {
        this.fieldName = name;
    }

}
