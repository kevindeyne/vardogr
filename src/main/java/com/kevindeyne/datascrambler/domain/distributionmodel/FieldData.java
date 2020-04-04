package com.kevindeyne.datascrambler.domain.distributionmodel;

import com.kevindeyne.datascrambler.domain.Generator;
import lombok.Data;

@Data
public class FieldData {

    private String fieldName;
    private Generator generator;
    private ValueDistribution valueDistribution;

    public FieldData(String name) {
        this.fieldName = name;
    }

}
