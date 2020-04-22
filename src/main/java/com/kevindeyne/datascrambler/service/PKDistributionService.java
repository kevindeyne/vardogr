package com.kevindeyne.datascrambler.service;

import com.google.common.collect.Sets;
import com.kevindeyne.datascrambler.domain.distributionmodel.FieldData;
import com.kevindeyne.datascrambler.domain.distributionmodel.TableData;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PKDistributionService {

    public List<Map<String, Object>> generatePrimaryKey(TableData table) {
        List<FieldData> pkData = getPKData(table);
        if(!pkData.isEmpty()) return generatePrimaryKey(pkData, table.getTotalCount());
        return new ArrayList<>();
    }

    private List<FieldData> getPKData(TableData table) {
        return table.getFieldData().parallelStream().filter(f-> f.isPrimaryKey() && f.getForeignKeyData() != null).collect(Collectors.toList());
    }

    List<Map<String, Object>> generatePrimaryKey(List<FieldData> fieldData, long size) {
        List<Map<String, Object>> pks = new ArrayList<>();
        final List<FieldData> lists = fieldData.stream().filter(FieldData::isPrimaryKey).collect(Collectors.toList());

        Set<?>[] array = new Set<?>[lists.size()];
        String[] fieldMapping = new String[lists.size()];

        for (int i = 0; i < lists.size(); i++) {
            array[i] = lists.get(i).getForeignKeyData().getPossibleValues();
            fieldMapping[i] = lists.get(i).getFieldName();
        }

        Set<List<Object>> sets = Sets.cartesianProduct(array);
        sets = sets.stream().filter(s-> s.size() == lists.size()).limit(size).collect(Collectors.toSet());
        for (List<Object> set : sets) {
            int index = 0;
            Map<String, Object> key = new HashMap<>();
            for (Object obj : set) {
                key.put(fieldMapping[index++], obj);
            }
            pks.add(key);
        }
        return pks;
    }
}
