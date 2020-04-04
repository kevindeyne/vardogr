package com.kevindeyne.datascrambler.domain.distributionmodel;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Generator {

    @SerializedName(value = "l")
    private int length;
    @SerializedName(value = "p")
    private int precision;
    @SerializedName(value = "o")
    private String originalType;

}
