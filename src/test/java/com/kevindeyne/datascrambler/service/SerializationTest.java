package com.kevindeyne.datascrambler.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jooq.DataType;
import org.jooq.impl.SQLDataType;
import org.junit.Test;

import java.util.Arrays;

public class SerializationTest {

    private Gson gson = new GsonBuilder().create();

    @Test
    public void testSerializeDataType() {
        DataType<?> dataType = SQLDataType.INTEGER;




        System.out.println(dataType.getTypeName());
    }

    @Test
    public void testMe() {
        String[] x = Arrays.asList("test", "another test", "something").toArray(new String[0]);
        System.out.println(x[0]);
        System.out.println(x[1]);
        System.out.println(x[2]);
    }

}
