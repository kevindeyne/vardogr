package com.kevindeyne.datascrambler.helper;

import org.fluttercode.datafactory.impl.DataFactory;

import java.sql.Date;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

class Scrambler {

    private static DataFactory f = new DataFactory();

    static String scrambleValue(String type, Integer length) {
        try{
            Class clazz = Class.forName(type);

            if(clazz.equals(Integer.class)){
                return f.getNumberText(length);
            } else if(clazz.equals(Date.class)){
                return f.getBirthDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().format(DateTimeFormatter.ISO_DATE);
            }

            return f.getRandomWord(length, false);
        }catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
