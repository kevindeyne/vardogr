package com.kevindeyne.datascrambler.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Generator {

    private int length;
    private int precision;
    private String originalType;

}
