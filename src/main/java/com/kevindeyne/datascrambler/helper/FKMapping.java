package com.kevindeyne.datascrambler.helper;

import com.kevindeyne.datascrambler.domain.Dependency;
import com.kevindeyne.datascrambler.domain.MConnection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FKMapping {

    public Map<String, Dependency> dependencyMap = new HashMap<>();
    Map<String, Map<String, String>> dValueMap = new HashMap<>();
    Map<String, List<String>> keys = new HashMap<>();

    public void handleDependenciesWithNoChildren(MConnection con, String db) {
        int handled = 0;

        List<Dependency> values = new ArrayList<>(dependencyMap.values());
        for(Dependency dep : values){
            if(!dep.getHandled()){
                int countChildren = 0;
                for(Dependency child : dep.getChildren()){
                    if(!child.getHandled()){
                        countChildren++;
                    }
                }

                if(countChildren == 0) {
                    String table = dep.getTable();

                    System.out.println();
                    System.out.println("Downloading: " + table);
                    if(con.getConnection() != null)
                        Copying.getData(con, table, db, keys.get(table));

                    dependencyMap.get(table).setHandled(true);
                    handled++;
                }
            }
        }

        if(handled > 0)
            handleDependenciesWithNoChildren(con, db);
    }

    public void addTable(String tableName, String columnName){
        /*if(dependencyMap.get(tableName) == null){
            Dependency dependency = new Dependency();
            dependency.setTable(tableName);
            dependency.setColumn(columnName);

            dependencyMap.put(tableName, dependency);
        }*/
    }

    public void addDependency(String fkTableName, String fkColumnName, String pkTableName, String pkColumnName) {
        Dependency child;
        Dependency parent;

        if(dependencyMap.get(String.format("%s.%s", pkTableName, pkColumnName)) != null){
            child = dependencyMap.get(pkTableName);
        } else {
            child = new Dependency();
            child.setTable(pkTableName);
            child.setColumn(pkColumnName);
        }

        if(dependencyMap.get(String.format("%s.%s", fkTableName, fkColumnName)) != null){
            parent = dependencyMap.get(fkTableName);
        } else {
            parent = new Dependency();
            parent.setTable(fkTableName);
            parent.setColumn(fkColumnName);
        }

        parent.getChildren().add(child);
        child.getParents().add(parent);

        dependencyMap.put(parent.getTable(), parent);
        dependencyMap.put(child.getTable(), child);
    }
}