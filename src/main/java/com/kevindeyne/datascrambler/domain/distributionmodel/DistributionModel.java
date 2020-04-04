package com.kevindeyne.datascrambler.domain.distributionmodel;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.synchronizedList;

@Data
public class DistributionModel {

    private List<TableData> tables = synchronizedList(new ArrayList<>());

    public JsonObject toJsonFile() {
        return JsonParser.parseString(new Gson().toJson(this)).getAsJsonObject();
    }
}
