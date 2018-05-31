package com.kevindeyne.datascrambler.helper;

import org.fluttercode.datafactory.impl.DataFactory;

import java.sql.Date;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Scrambler {

    private static DataFactory f = new DataFactory();

    static String consistentValue(Class clazz, Integer rowNr, Long tableSize, boolean canHaveNull){
        if(rowNr > tableSize) {
            if(canHaveNull){
                return "NULL";
            } else {
              rowNr = (int)(Math.random() * (tableSize.intValue())) + 1;
            }
        }

        if(clazz.equals(Integer.class)){
            return rowNr.toString();
        }

        return String.format("'%s'", rowNr.toString());
    }

    static String scrambleValue(Class clazz, String columnName, Integer length) {
        if(null != clazz) {
            if(clazz.equals(Integer.class)){
                return f.getNumberText(length);
            } else if(clazz.equals(Date.class)){
                return String.format("'%s'", f.getBirthDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().format(DateTimeFormatter.ISO_DATE));
            }
        }

        if(columnName.toLowerCase().contains("name")){
            return nameGen(columnName, length);
        } else {
            return String.format("'%s'", getRandomText(length));
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
            result = sb.toString().trim();
        } else if(length == 0) {
            result = StatementBuilder.EMPTY;
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
