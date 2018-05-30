package com.kevindeyne.datascrambler.helper;

import com.kevindeyne.datascrambler.domain.Config;
import com.kevindeyne.datascrambler.domain.MConnection;
import com.kevindeyne.datascrambler.domain.Table;

import java.sql.*;

public class Copying {

    static FKMapping fk = new FKMapping();

    public static void downloadDatabase(Config obj, String db) {
        try {
            MConnection mConnection = obj.buildConnection(db);
            loadKeysForTables(mConnection.getConnection(), db);

            while(fk.hasNext()){
                Copying.getData(mConnection, fk.next(), db);
            }
        } catch(SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void loadKeysForTables(Connection connection, String db) throws SQLException {
        ResultSet rs = getAllTables(connection);
        while (rs.next()) {
            KeyBuilder.getKeys(connection, getTableName(rs), db);
        }
    }

    private static ResultSet getAllTables(Connection connection) throws SQLException {
        return connection.getMetaData().getTables(connection.getCatalog(), connection.getSchema(), "%", null);
    }

    private static String getTableName(ResultSet rs) throws SQLException {
        return rs.getString(3);
    }

    static Long countFKs(Connection connection, String db, String fkTableName, String fkColumnName, String pkTableName, String pkColumnName) throws SQLException {
        String countSql = "SELECT count(*) FROM `%s`.`%s` o INNER JOIN `%s`.`%s` p on p.%s = o.%s";
        countSql = String.format(countSql, db, fkTableName, db, pkTableName, pkColumnName, fkColumnName);
        Long amountOfCouplings;
        try (PreparedStatement selectStmt = connection.prepareStatement(countSql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY); ResultSet rs = selectStmt.executeQuery()) {
            if (rs.next()) {
                amountOfCouplings = rs.getLong(1);
            } else {
                amountOfCouplings = -1L;
            }
        }
        return amountOfCouplings;
    }

    private static void getData(MConnection con, Table table, String db){
        try {
            String selectAll = String.format("SELECT * FROM `%s`.`%s`", db, table.getName());
            try (PreparedStatement selectStmt = con.getConnection().prepareStatement(selectAll, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY); ResultSet rs = selectStmt.executeQuery()) {
                if (!rs.isBeforeFirst()) {
                    System.out.println("No rows found");
                } else {
                    String createQuery = null;
                    while (rs.next()) {
                        if (createQuery == null) {
                            createQuery = StatementBuilder.buildCreateTableStatement(table, rs, db);
                            System.out.println();
                            System.out.println(createQuery);
                        }

                        String insert = StatementBuilder.buildInsertStatement(table, db, rs);
                        System.out.println(insert);
                    }
                }
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }

        //throw new RuntimeException("Stopping");
    }
}