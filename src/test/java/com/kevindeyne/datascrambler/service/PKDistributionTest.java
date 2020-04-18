package com.kevindeyne.datascrambler.service;

import com.kevindeyne.datascrambler.domain.distributionmodel.FieldData;
import com.kevindeyne.datascrambler.domain.distributionmodel.ForeignKeyData;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PKDistributionTest {

    @Test
    public void testDistributionComboPKAsFK_2fields() {
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

        final List<Map<String, Object>> pks = generatePrimaryKey(fieldData, 5);
        pks.forEach(System.out::println);

        Assert.assertEquals(5, pks.size());
        for(Map<String, Object> key : pks) {
            Assert.assertFalse(isInteger(key.get("actor_id").toString()));
            Assert.assertTrue(isInteger(key.get("film_id").toString()));
        }
    }

    @Test
    public void testDistributionComboPKAsFK_3fields() {
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

        final List<Map<String, Object>> pks = generatePrimaryKey(fieldData, 10);
        pks.forEach(System.out::println);

        Assert.assertEquals(10, pks.size());
        for(Map<String, Object> key : pks) {
            Assert.assertFalse(isInteger(key.get("otherID").toString()));
            Assert.assertFalse(isInteger(key.get("actor_id").toString()));
            Assert.assertTrue(isInteger(key.get("film_id").toString()));
        }
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

    private List<Map<String, Object>> generatePrimaryKey(List<FieldData> fieldData, int size) {
        List<Map<String, Object>> pks = new ArrayList<>();
        final List<FieldData> lists = fieldData.stream().filter(FieldData::isPrimaryKey).collect(Collectors.toList());
        Set<?>[] array = new Set<?>[lists.size()];
        String[] fieldMapping = new String[lists.size()];
        for (int i = 0; i < lists.size(); i++) {
            array[i] = lists.get(i).getForeignKeyData().getPossibleValues();
            fieldMapping[i] = lists.get(lists.size()-i-1).getFieldName();
        }

        final Set<Set<Object>> sets = _cartesianProduct(0, size, array);
        for (Set<Object> set : sets) {
            int index = 0;
            Map<String, Object> key = new HashMap<>();
            for (Object obj : set) {
                key.put(fieldMapping[index++], obj);
            }
            pks.add(key);
        }
        return pks;
    }

    private static Set<Set<Object>> _cartesianProduct(int index, int limit, Set<?>... sets) {
        Set<Set<Object>> ret = new LinkedHashSet<>();
        if (index == sets.length) {
            ret.add(new LinkedHashSet<>());
        } else {
            for (Object obj : sets[index]) {
                int innerIndex = index + 1;
                for (Set<Object> set : _cartesianProduct(innerIndex, limit, sets)) {
                    set.add(obj);
                    ret.add(set);
                    if(ret.size() == limit) return ret;
                }
            }
        }
        return ret;
    }

}
