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
        MConnection mConnection = null;
        try {
            fk = new FKMapping();
            mConnection = obj.buildConnection(db);
            loadKeysForTables(mConnection);
            Filewriter.cleanFolder();
            while(fk.hasNext()){
                Copying.getData(mConnection, fk.next());
            }
            ProgressBar.finish(totalRecordsToDownload);
        } catch(SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try{
                if(null != mConnection)
                    mConnection.getConnection().close();
            } catch(SQLException e) {
                e.printStackTrace();
            }
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
            ResultSet resultSet = metadata.getColumns(con.getCatalog(), con.getSchema(), table.getName(), null);
            int tableNr = fk.getTableNr(table);

            String createSQL = StatementBuilder.buildCreateTableStatement(table, resultSet, con.getDb());
            Filewriter.writeToFile(createSQL, con.getDb(), tableNr);
            for (int i = 1; i < table.getTableSize() + 1; i++) {
                String insertSQL = StatementBuilder.buildInsertStatement(table, con.getDb(), i);
                Filewriter.writeToFile(insertSQL, con.getDb(), tableNr);
                ProgressBar.step(++currentDownloadedRecords, totalRecordsToDownload);
            }

            for(ForeignKey key : table.getFks()){
                String fkSQL = StatementBuilder.buildFKStatement(table, key, con.getDb());
                Filewriter.writeToFile(fkSQL, con.getDb(), tableNr);
            }
        } catch (Exception e) {
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