package com.kevindeyne.datascrambler.domain.distributionmodel;

import com.grack.nanojson.JsonObject;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.synchronizedList;

@Data
public class DistributionModel {

    private List<TableData> tables = synchronizedList(new ArrayList<>());

    public JsonObject toJsonFile() {
        return new JsonObject();
    }
}
