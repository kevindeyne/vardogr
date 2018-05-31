package com.kevindeyne.datascrambler.helper;

import com.kevindeyne.datascrambler.domain.ForeignKey;
import com.kevindeyne.datascrambler.domain.MConnection;
import com.kevindeyne.datascrambler.domain.Table;

import java.sql.*;
import java.util.Collection;

class KeyBuilder {

    private static final String COL = "COL";

    static void getKeys(MConnection connection, String table) throws SQLException {
        ResultSet primaryKeys = connection.getPrimaryKeys(table);
        while(primaryKeys.next()) {
            ResultSetMetaData metaData = primaryKeys.getMetaData();

            for (int i = 1; i < metaData.getColumnCount() + 1; i++) {
                if(isColumn(metaData, i)){
                    Table loadedTable = Copying.addTable(table, primaryKeys.getObject(i).toString());
                    appendTableSizeToTable(connection, loadedTable);
                }
            }
        }

        loadFKs(connection, table);
    }

    private static void appendTableSizeToTable(MConnection connection, Table loadedTable) {
        loadedTable.setTableSize(connection.countTableSize(loadedTable.getName()));
        Copying.addRecordCount(loadedTable.getTableSize());
    }

    private static boolean isColumn(ResultSetMetaData metaData, int i) throws SQLException{
        return metaData.getColumnName(i).contains(COL);
    }

    private static void loadFKs(MConnection c, String table) throws SQLException {
        ResultSet fks = c.getConnection()
                .getMetaData()
                .getImportedKeys(c.getCatalog(), c.getSchema(), table);
        while (fks.next()) {
            String fkTableName = fks.getString(ForeignKey.FKTABLE_NAME);
            String fkColumnName = fks.getString(ForeignKey.FKCOLUMN_NAME);
            String pkTableName = fks.getString(ForeignKey.PKTABLE_NAME);
            String pkColumnName = fks.getString(ForeignKey.PKCOLUMN_NAME);

            Copying.addDependency(fkTableName, fkColumnName, pkTableName, pkColumnName);
        }
    }

    static String buildPrimaryKeys(Collection<String> keys){
        if(keys.size() == 1) {
            return keys.iterator().next();
        } else {
            StringBuilder sb = new StringBuilder();
            String appender = StatementBuilder.EMPTY;
            for(String key : keys){
                sb.append(appender);
                sb.append(key);
                appender = StatementBuilder.COMMA;
            }
            return sb.toString();
        }
    }
}