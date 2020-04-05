package com.kevindeyne.datascrambler.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jooq.DataType;
import org.jooq.impl.SQLDataType;
import org.junit.Test;

public class SerializationTest {

    private Gson gson = new GsonBuilder().create();

    @Test
    public void testSerializeDataType() {
        DataType<?> dataType = SQLDataType.INTEGER;




        System.out.println(dataType.getTypeName());
    }

}
