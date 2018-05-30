package com.kevindeyne.datascrambler.helper;

import java.sql.*;
import java.util.Collection;

class KeyBuilder {

    static void getKeys(Connection connection, String table, String db) throws SQLException {
        DatabaseMetaData md = connection.getMetaData();
        String catalog = connection.getCatalog();
        String schema = connection.getSchema();

        ResultSet primaryKeys = md.getPrimaryKeys(catalog, schema, table);
        while(primaryKeys.next()) {
            ResultSetMetaData metaData = primaryKeys.getMetaData();

            for (int i = 1; i < metaData.getColumnCount() + 1; i++) {
                String colName = metaData.getColumnName(i);
                if(colName.contains("COL")){
                    Copying.fk.addTable(table, primaryKeys.getObject(i).toString());
                }
            }
        }

        loadFKs(connection, table, db);
    }

    private static void loadFKs(Connection connection, String table, String db) throws SQLException {
        ResultSet foreignKeys = connection.getMetaData().getImportedKeys(connection.getCatalog(), connection.getSchema(), table);
        while (foreignKeys.next()) {
            String fkTableName = foreignKeys.getString("FKTABLE_NAME");
            String fkColumnName = foreignKeys.getString("FKCOLUMN_NAME");
            String pkTableName = foreignKeys.getString("PKTABLE_NAME");
            String pkColumnName = foreignKeys.getString("PKCOLUMN_NAME");

            Long amountOfCouplings = Copying.countFKs(connection, db, fkTableName, fkColumnName, pkTableName, pkColumnName);
            Long tableSize = Copying.countTableSize(connection, db, pkTableName);

            Copying.fk.addDependency(fkTableName, fkColumnName, pkTableName, pkColumnName, amountOfCouplings, tableSize);
        }
    }

    static String buildPrimaryKeys(Collection<String> keys){
        if(keys.size() == 1) {
            return keys.iterator().next();
        } else {
            StringBuilder sb = new StringBuilder();
            String appender = "";
            for(String key : keys){
                sb.append(appender);
                sb.append(key);
                appender = ",";
            }
            return sb.toString();
        }
    }
}