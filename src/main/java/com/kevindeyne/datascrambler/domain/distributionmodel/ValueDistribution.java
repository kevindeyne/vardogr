package com.kevindeyne.datascrambler.domain.distributionmodel;

import lombok.Data;
import org.jooq.Record2;
import org.jooq.Result;

import java.util.ArrayList;
import java.util.List;

@Data
public class ValueDistribution {

    private List<Double> percentages = new ArrayList<>();

    public ValueDistribution from(Result<? extends Record2<?, Integer>> rawDistribution, long totalCount) {
        Double count = (double) totalCount;
        for(Record2<?, Integer> record : rawDistribution) {
            this.percentages.add(calculatePercentage(count, record));
        }
        return this;
    }

    private Double calculatePercentage(Double count, Record2<?, Integer> record) {
        return (record.component2()/count)*100D;
    }
}
