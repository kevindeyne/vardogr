package com.kevindeyne.datascrambler.service;

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
            fieldMapping[i] = lists.get(lists.size() - i - 1).getFieldName();
        }

        final Set<Set<Object>> sets = cartesianProduct(0, size, lists.size(), array);
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

    private Set<Set<Object>> cartesianProduct(int index, long limit, final int expectedSets, Set<?>... sets) {
        Set<Set<Object>> ret = new LinkedHashSet<>();
        if (index == sets.length) {
            ret.add(new LinkedHashSet<>());
        } else {
            for (Object obj : sets[index]) {
                int innerIndex = index + 1;
                for (Set<Object> set : cartesianProduct(innerIndex, limit, expectedSets, sets)) {
                    set.add(obj);
                    ret.add(set);
                    if (ret.stream().filter(r -> r.size() == expectedSets).count() == limit) return ret;
                }
            }
        }
        return ret;
    }
}
