package com.kevindeyne.datascrambler.domain.distributionmodel;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class IndexData {

    @SerializedName(value = "n")
    private String name;
    @SerializedName(value = "f")
    private List<String> fields = new ArrayList<>();
    @SerializedName(value = "u")
    private boolean unique;

}
