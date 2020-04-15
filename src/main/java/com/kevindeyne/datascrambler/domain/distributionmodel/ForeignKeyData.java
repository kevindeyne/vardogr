package com.kevindeyne.datascrambler.domain.distributionmodel;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ForeignKeyData {

    @SerializedName(value = "t")
    private String table;
    @SerializedName(value = "k")
    private String key;

}
