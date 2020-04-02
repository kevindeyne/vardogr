package com.kevindeyne.datascrambler.domain;

import org.jooq.Record2;
import org.jooq.Result;

import java.util.ArrayList;
import java.util.List;

public class Distribution {

    List<Double> percentages = new ArrayList<>();

    public static Distribution from(Result<? extends Record2<?, Integer>> rawDistribution, Double totalCount) {
        Distribution distribution = new Distribution();

        for(Record2<?, Integer> record : rawDistribution) {
            Double percentage = (record.component2()/totalCount)*100D;
            distribution.percentages.add(percentage);
        }

        return distribution;
    }

    @Override
    public String toString() {
        return "Distr{total=" + percentages.size() + (!percentages.isEmpty() ? " - " + percentages.get(0).toString() + "%" : "") + "}";
    }
}
