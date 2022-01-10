package com.kevindeyne.datascrambler.service;

import com.kevindeyne.datascrambler.domain.distributionmodel.FieldData;
import com.kevindeyne.datascrambler.domain.distributionmodel.ForeignKeyData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class PKDistributionTest {

    private final PKDistributionService service = new PKDistributionService();

    @Test
    void testDistributionComboPKAsFK_1field() {
        List<FieldData> fieldData = new ArrayList<>();

        FieldData f1 = new FieldData("actor_id");
        f1.setPrimaryKey(true);
        ForeignKeyData fkd = new ForeignKeyData();
        fkd.setPossibleValues(Stream.of("A", "B", "C", "D", "E").collect(Collectors.toSet()));
        f1.setForeignKeyData(fkd);
        fieldData.add(f1);

        final List<Map<String, Object>> pks = service.generatePrimaryKey(fieldData, 3);
        pks.forEach(System.out::println);

        Assertions.assertEquals(3, pks.size());
        for(Map<String, Object> key : pks) {
            Assertions.assertFalse(isInteger(key.get("actor_id").toString()));
            Assertions.assertEquals(1, key.size());
        }
    }

    @Test
    void testDistributionComboPKAsFK_2fields() {
        List<FieldData> fieldData = new ArrayList<>();

        FieldData f1 = new FieldData("actor_id");
        f1.setPrimaryKey(true);
        ForeignKeyData fkd = new ForeignKeyData();
        fkd.setPossibleValues(Stream.of("A", "B", "C", "D", "E").collect(Collectors.toSet()));
        f1.setForeignKeyData(fkd);
        fieldData.add(f1);

        FieldData f2 = new FieldData("film_id");
        f2.setPrimaryKey(true);
        ForeignKeyData fkd2 = new ForeignKeyData();
        fkd2.setPossibleValues(Stream.of(1, 2, 3).collect(Collectors.toSet()));
        f2.setForeignKeyData(fkd2);
        fieldData.add(f2);

        final List<Map<String, Object>> pks = service.generatePrimaryKey(fieldData, 5);
        pks.forEach(System.out::println);

        Assertions.assertEquals(5, pks.size());
        for(Map<String, Object> key : pks) {
            Assertions.assertFalse(isInteger(key.get("actor_id").toString()));
            Assertions.assertTrue(isInteger(key.get("film_id").toString()));
            Assertions.assertEquals(2, key.size());
        }
    }

    @Test
    void testDistributionComboPKAsFK_2fields_nolimit() {
        List<FieldData> fieldData = new ArrayList<>();

        FieldData f1 = new FieldData("actor_id");
        f1.setPrimaryKey(true);
        ForeignKeyData fkd = new ForeignKeyData();
        fkd.setPossibleValues(Stream.of("A", "B", "C", "D", "E").collect(Collectors.toSet()));
        f1.setForeignKeyData(fkd);
        fieldData.add(f1);

        FieldData f2 = new FieldData("film_id");
        f2.setPrimaryKey(true);
        ForeignKeyData fkd2 = new ForeignKeyData();
        fkd2.setPossibleValues(Stream.of(1, 2, 3).collect(Collectors.toSet()));
        f2.setForeignKeyData(fkd2);
        fieldData.add(f2);

        final List<Map<String, Object>> pks = service.generatePrimaryKey(fieldData, 5000);
        pks.forEach(System.out::println);

        Assertions.assertEquals(15, pks.size());
        for(Map<String, Object> key : pks) {
            Assertions.assertFalse(isInteger(key.get("actor_id").toString()));
            Assertions.assertTrue(isInteger(key.get("film_id").toString()));
            Assertions.assertEquals(2, key.size());
        }
    }

    @Test
    void testDistributionComboPKAsFK_3fields() {
        List<FieldData> fieldData = new ArrayList<>();

        FieldData f2 = new FieldData("film_id");
        f2.setPrimaryKey(true);
        ForeignKeyData fkd2 = new ForeignKeyData();
        fkd2.setPossibleValues(Stream.of(1, 2, 3).collect(Collectors.toSet()));
        f2.setForeignKeyData(fkd2);
        fieldData.add(f2);

        FieldData f1 = new FieldData("actor_id");
        f1.setPrimaryKey(true);
        ForeignKeyData fkd = new ForeignKeyData();
        fkd.setPossibleValues(Stream.of("A", "B", "C", "D", "E").collect(Collectors.toSet()));
        f1.setForeignKeyData(fkd);
        fieldData.add(f1);

        FieldData f3 = new FieldData("otherID");
        f3.setPrimaryKey(true);
        ForeignKeyData fkd3 = new ForeignKeyData();
        fkd3.setPossibleValues(Stream.of("*", "-", "/", "+").collect(Collectors.toSet()));
        f3.setForeignKeyData(fkd3);
        fieldData.add(f3);

        final List<Map<String, Object>> pks = service.generatePrimaryKey(fieldData, 10);
        pks.forEach(System.out::println);

        Assertions.assertEquals(10, pks.size());
        for(Map<String, Object> key : pks) {
            Assertions.assertFalse(isInteger(key.get("otherID").toString()));
            Assertions.assertFalse(isInteger(key.get("actor_id").toString()));
            Assertions.assertTrue(isInteger(key.get("film_id").toString()));
            Assertions.assertEquals(3, key.size());
        }
    }

    @Test
    void testDistributionComboPKAsFK_3fields_massive() {
        List<FieldData> fieldData = new ArrayList<>();

        Set<Object> fkList = new HashSet<>();
        Set<Object> fk2List = new HashSet<>();
        Set<Object> fk3List = new HashSet<>();

        for (int i = 0; i < 123 + new SecureRandom().nextInt(99999); i++) fkList.add(i);
        for (int i = 0; i < 234 + new SecureRandom().nextInt(99999); i++) fk2List.add(i);
        for (int i = 0; i < 456 + new SecureRandom().nextInt(99999); i++) fk3List.add(i);

        FieldData f2 = new FieldData("film_id");
        f2.setPrimaryKey(true);
        ForeignKeyData fkd2 = new ForeignKeyData();
        fkd2.setPossibleValues(fkList);
        f2.setForeignKeyData(fkd2);
        fieldData.add(f2);

        FieldData f1 = new FieldData("actor_id");
        f1.setPrimaryKey(true);
        ForeignKeyData fkd = new ForeignKeyData();
        fkd.setPossibleValues(fk2List);
        f1.setForeignKeyData(fkd);
        fieldData.add(f1);

        FieldData f3 = new FieldData("otherID");
        f3.setPrimaryKey(true);
        ForeignKeyData fkd3 = new ForeignKeyData();
        fkd3.setPossibleValues(fk3List);
        f3.setForeignKeyData(fkd3);
        fieldData.add(f3);

        final List<Map<String, Object>> pks = service.generatePrimaryKey(fieldData, 100000);
    }

    public boolean isInteger(String s) {
        if(s.isEmpty()) return false;
        for(int i = 0; i < s.length(); i++) {
            if(i == 0 && s.charAt(i) == '-') {
                if(s.length() == 1) return false;
                else continue;
            }
            if(Character.digit(s.charAt(i),10) < 0) return false;
        }
        return true;
    }
}
