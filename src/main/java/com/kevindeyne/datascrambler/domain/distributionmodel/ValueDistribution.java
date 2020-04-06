package com.kevindeyne.datascrambler.domain.distributionmodel;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jooq.Record2;
import org.jooq.Result;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ValueDistribution {

    @SerializedName(value = "p")
    private Map<Double, MutableInt> percentages = new HashMap<>();

    @NoArgsConstructor
    @AllArgsConstructor
    public static class MutableInt {
        private int v = 1;
        public void increment () { ++v;}
        public int get() { return v; }
    }

    public ValueDistribution from(Result<? extends Record2<?, Integer>> rawDistribution, long totalCount) {
        Double count = (double) totalCount;
        for(Record2<?, Integer> record : rawDistribution) {
            final Double percentage = calculatePercentage(count, record);
            MutableInt pCount = percentages.get(percentage);
            if (pCount == null) percentages.put(percentage, new MutableInt());
            else pCount.increment();
        }
        return this;
    }

    private Double calculatePercentage(Double count, Record2<?, Integer> record) {
        double fullPrecision = (record.component2()/count)*100D;
        return BigDecimal.valueOf(fullPrecision).setScale(4, RoundingMode.HALF_UP).doubleValue();
    }
}
