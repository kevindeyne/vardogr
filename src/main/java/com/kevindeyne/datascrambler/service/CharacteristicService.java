package com.kevindeyne.datascrambler.service;

import com.kevindeyne.datascrambler.domain.distributionmodel.Characteristics;
import org.jooq.DSLContext;
import org.jooq.Record2;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.sql.Date;
import java.util.List;

import static org.jooq.impl.DSL.*;

@Service
public class CharacteristicService {

    public List<String> determineCharacteristics(Long minValue, Long maxValue) {
        List<String> characteristics = new ArrayList<>();
        if (minValue < 0) characteristics.add(Characteristics.CAN_BE_NEGATIVE_NUMBER.getShortcutValue());
        if (maxValue > 0) characteristics.add(Characteristics.CAN_BE_POSITIVE_NUMBER.getShortcutValue());
        return characteristics;
    }

    public List<String> determineCharacteristics(Date minValue, Date maxValue) {
        List<String> characteristics = new ArrayList<>();
        Date now = new Date(System.currentTimeMillis());
        if (minValue.before(now)) characteristics.add(Characteristics.CAN_BE_DATE_IN_PAST.getShortcutValue());
        if (maxValue.after(now)) characteristics.add(Characteristics.CAN_BE_DATE_IN_FUTURE.getShortcutValue());
        return characteristics;
    }

    public List<String> determineCharacteristics(DSLContext dsl, String tableName, String fieldName, String originalType) {
        final Record2<Object, Object> minMaxValues = getMinMaxValues(dsl, tableName, fieldName);
        if (originalType.equals(Integer.class.getName()) || originalType.equals(Short.class.getName()) || originalType.equals(Long.class.getName())){
            return determineCharacteristics(Long.valueOf(minMaxValues.value1().toString()), Long.valueOf(minMaxValues.value2().toString()));
        } else if (originalType.equals(Timestamp.class.getName())) {
            Date dMin = new Date(((Timestamp) minMaxValues.value1()).getTime());
            Date dMax = new Date(((Timestamp) minMaxValues.value2()).getTime());
            return determineCharacteristics(dMin, dMax);
        } else if (originalType.equals(Date.class.getName())) {
            return determineCharacteristics((Date) minMaxValues.value1(), (Date) minMaxValues.value2());
        }
        return null; //do not return an empty collection here, because that would translate to a [] in the model as opposed to being removed for being NULL
    }

    private Record2<Object, Object> getMinMaxValues(DSLContext dsl, String tableName, String fieldName) {
        return dsl.select(max(field(quotedName(fieldName))), min(field(quotedName(fieldName)))).from(table(quotedName(tableName))).fetchAny();
    }

    public boolean supported(String originalType) {
        return originalType.equals(Integer.class.getName()) || originalType.equals(Short.class.getName()) || originalType.equals(Long.class.getName()) ||
                originalType.equals(Timestamp.class.getName()) || originalType.equals(Date.class.getName());
    }
}