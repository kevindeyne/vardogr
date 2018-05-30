package com.kevindeyne.datascrambler.helper;

import com.kevindeyne.datascrambler.domain.ForeignKey;
import com.kevindeyne.datascrambler.domain.Table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FKMapping {

    private List<Table> loaded;
    private List<Table> handled;

    public FKMapping(){
        loaded = new ArrayList<>();
        handled = new ArrayList<>();
    }

    public Table addTable(String tableName, String ... columnNames){
        Table table = new Table(tableName);

        int index = Collections.binarySearch(loaded, table, Comparator.comparing(Table::getName));
        if(index < 0){
            if(columnNames != null){
                for(String columnName : columnNames) {
                    table.getPks().add(columnName);
                }
            }

            loaded.add(table);
            Collections.sort(loaded, Comparator.comparing(Table::getName));
        } else {
            return loaded.get(index);
        }

        return table;
    }

    public ForeignKey addDependency(String referencingTableName, String referencingKey, String referencedTableName, String referencedKey, Long amountOfFKs) {
        Table referencingTable = addTable(referencingTableName);
        Table referencedTable = addTable(referencedTableName, referencedKey);

        ForeignKey fk = new ForeignKey(referencedTable, referencingKey, amountOfFKs);
        referencingTable.getFks().add(fk);
        return fk;
    }

    public Table next() {
        if(handled.isEmpty()){
            Collections.sort(loaded, Comparator.comparing(t -> t.getFks().size()));
            Table first = loaded.get(0);
            handled.add(first);
            return first;
        } else {
            for(Table loadedTable : loaded){
                if(!handled.contains(loadedTable)){
                    handled.add(loadedTable);
                    return loadedTable;
                }
            }
        }

        return null;
    }

    public boolean hasNext(){
        return handled.size() < loaded.size();
    }
}