package com.kevindeyne.datascrambler.helper;

import org.fluttercode.datafactory.impl.DataFactory;

import java.sql.Date;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Scrambler {

    private static DataFactory f = new DataFactory();

    static String consistentValue(String type, Integer rowNr, Long max, Long tableSize, boolean nullable){ //TODO use max to calculate fill rate and simulate
        if(rowNr > tableSize) {
            if(nullable){
                return "NULL";
            } else {
              rowNr = (int)(Math.random() * (tableSize.intValue())) + 1;
            }
        }

        try{
            Class clazz = Class.forName(type);

            if(clazz.equals(Integer.class)){
                return rowNr.toString();
            }

            return String.format("'%s'", rowNr.toString());
        }catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    static String scrambleValue(String columnName, String type, Integer length) {
        try{
            Class clazz = Class.forName(type);

            if(clazz.equals(Integer.class)){
                return f.getNumberText(length);
            } else if(clazz.equals(Date.class) || clazz.equals(java.util.Date.class)){
                return String.format("'%s'", f.getBirthDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().format(DateTimeFormatter.ISO_DATE));
            }

            if(columnName.toLowerCase().contains("name")){
                return nameGen(columnName, length);
            } else {
                return String.format("'%s'", getRandomText(length));
            }
        }catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getRandomText(Integer length) {
        String result;

        int div = length/10;
        if(length > 10){
            int length2 = length-div;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < div; i++) {
                sb.append(f.getRandomText(1, length2/div));
                sb.append(" ");
            }
            result = sb.toString();
        } else {
            result = f.getRandomText(1, length);
        }
        result = result.replace("'", "");
        return result;
    }

    private static String nameGen(String columnName, Integer length) {
        String name = f.getLastName();

        if(columnName.contains("first") || columnName.contains("voor") || columnName.contains("pre")){
            name = f.getFirstName();
        }

        name = name.replace("'", "-");

        if(name.length() > length){
            name = name.substring(0, length);
        }
        return String.format("'%s'", name);
    }
}
