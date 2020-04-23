package com.kevindeyne.datascrambler.domain.distributionmodel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
public class ForeignKeyData {

    @SerializedName(value = "t")
    private String table;
    @SerializedName(value = "k")
    private String key;
    @Expose(serialize = false)
    private Set<Object> possibleValues;
    @Expose(serialize = false)
    private Integer offset;

    public ForeignKeyData(String table, String key) {
        this.table = table;
        this.key = key;
    }

}
