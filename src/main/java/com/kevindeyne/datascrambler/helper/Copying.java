package com.kevindeyne.datascrambler.helper;

import com.kevindeyne.datascrambler.domain.Config;
import com.kevindeyne.datascrambler.domain.ForeignKey;
import com.kevindeyne.datascrambler.domain.MConnection;
import com.kevindeyne.datascrambler.domain.Table;

import java.sql.*;

public class Copying {

    private static FKMapping fk = new FKMapping();
    private static Long totalRecordsToDownload = 0L;
    private static Long currentDownloadedRecords = 0L;

    public static void downloadDatabase(Config obj, String db) {
        try {
            fk = new FKMapping();
            MConnection mConnection = obj.buildConnection(db);
            loadKeysForTables(mConnection);

            while(fk.hasNext()){
                Copying.getData(mConnection, fk.next());
            }
            System.out.print("Processing: " + totalRecordsToDownload + " / " + totalRecordsToDownload + "\r");
            System.out.println();

            mConnection.getConnection().close();
        } catch(SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void loadKeysForTables(MConnection connection) throws SQLException {
        totalRecordsToDownload = 0L;
        currentDownloadedRecords = 0L;
        ResultSet rs = getAllTables(connection);
        while (rs.next()) {
            String tableName = getTableName(rs);
            KeyBuilder.getKeys(connection, tableName);
        }
    }

    private static ResultSet getAllTables(MConnection connection) throws SQLException {
        return connection.getConnection().getMetaData().getTables(connection.getCatalog(), connection.getSchema(), "%", null);
    }

    private static String getTableName(ResultSet rs) throws SQLException {
        return rs.getString(3);
    }

    private static void getData(MConnection con, Table table){
        try {
            DatabaseMetaData metadata = con.getConnection().getMetaData();
            ResultSet rs = metadata.getColumns(con.getCatalog(), con.getSchema(), table.getName(), null);

            if (rs.next()) {
                StatementBuilder.buildCreateTableStatement(table, rs, con.getDb());

                for (int i = 0; i < table.getTableSize(); i++) {
                    StatementBuilder.buildInsertStatement(table, con.getDb(), rs);

                    if(++currentDownloadedRecords % 101 == 0){
                        System.out.print("Processing: " + currentDownloadedRecords + " / " + totalRecordsToDownload + "\r");
                    }
                }
            }

            for(ForeignKey key : table.getFks()){
                StatementBuilder.buildFKStatement(table, key, con.getDb());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static void reset(){
        fk = null;
        totalRecordsToDownload = 0L;
        currentDownloadedRecords = 0L;
        System.gc();
    }

    public static void addRecordCount(Long tableSize) {
        totalRecordsToDownload += tableSize;
    }

    public static Table addTable(String tableName, String ... columnNames){
        return fk.addTable(tableName, columnNames);
    }

    public static void addDependency(String fkTableName, String fkColumnName, String pkTableName, String pkColumnName) {
        fk.addDependency(fkTableName, fkColumnName, pkTableName, pkColumnName);
    }
}