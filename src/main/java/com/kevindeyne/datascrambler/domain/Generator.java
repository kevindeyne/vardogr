package com.kevindeyne.datascrambler.domain;

import org.jooq.Field;

public class Generator {

    private int length;
    private int precision;
    private String originalType;

    public Generator(String originalType, int length, int precision) {
        this.length = length;
        this.originalType = originalType;
        this.precision = precision;
    }

    public static Generator determineGenerator(Field<?> f) {
        String type = f.getDataType().getType().getTypeName();
        int length = f.getDataType().length();
        int precision = f.getDataType().precision();

        return new Generator(type, length, precision);
    }

    public int getLength() {
        return length;
    }

    public String getOriginalType() {
        return originalType;
    }

    public int getPrecision() {
        return precision;
    }

    @Override
    public String toString() {
        return "Generator{" +
                "length=" + length +
                ", precision=" + precision +
                ", originalType='" + originalType + '\'' +
                '}';
    }
}
