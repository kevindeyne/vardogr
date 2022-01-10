package com.kevindeyne.datascrambler.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jooq.DataType;
import org.jooq.impl.SQLDataType;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

class SerializationTest {

    private final Gson gson = new GsonBuilder().create();

    @Test
    void testSerializeDataType() {
        DataType<?> dataType = SQLDataType.INTEGER;

        System.out.println(dataType.getTypeName());
    }

    @Test
    void testMe() {
        String[] x = Arrays.asList("test", "another test", "something").toArray(new String[0]);
        System.out.println(x[0]);
        System.out.println(x[1]);
        System.out.println(x[2]);
    }

}
