package com.kevindeyne.datascrambler.helper;

import com.kevindeyne.datascrambler.domain.Config;
import com.kevindeyne.datascrambler.domain.Dependency;
import com.kevindeyne.datascrambler.domain.MConnection;
import com.kevindeyne.datascrambler.domain.Table;
import org.springframework.util.CollectionUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.kevindeyne.datascrambler.helper.Scrambler.scrambleValue;

public class Copying {
    //TODO move to config?
    static String CHARSET = "utf8";
    static String ENGINE = "InnoDB";
    static String COLLATE = "utf8_bin";

    private static FKMapping fk = new FKMapping();

    public static void downloadDatabase(Config obj, String db) {
        try {
            MConnection mConnection = obj.buildConnection(db);
            loadKeysForTables(mConnection.getConnection());

            while(fk.hasNext()){
                Copying.getData(mConnection, fk.next(), db);
            }
        } catch(SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void loadKeysForTables(Connection connection) throws SQLException {
        DatabaseMetaData md = connection.getMetaData();
        String catalog = connection.getCatalog();
        String schema = connection.getSchema();

        ResultSet rs = getAllTables(md, catalog, schema);
        while (rs.next()) {
            Copying.getKeys(md, getTableName(rs), catalog, schema);
        }
    }

    private static ResultSet getAllTables(DatabaseMetaData md, String catalog, String schema) throws SQLException {
        return md.getTables(catalog, schema, "%", null);
    }

    private static String getTableName(ResultSet rs) throws SQLException {
        return rs.getString(3);
    }

    private static void getKeys(DatabaseMetaData md, String table, String catalog, String schema) throws SQLException {
        ResultSet primaryKeys = md.getPrimaryKeys(catalog, schema, table);
        while(primaryKeys.next()) {
            ResultSetMetaData metaData = primaryKeys.getMetaData();

            for (int i = 1; i < metaData.getColumnCount() + 1; i++) {
                String colName = metaData.getColumnName(i);
                if(colName.contains("COL")){
                    fk.addTable(table, primaryKeys.getObject(i).toString());
                }
            }
        }

        loadFKs(md, table, catalog, schema);
    }

    private static void loadFKs(DatabaseMetaData md, String table, String catalog, String schema) throws SQLException {
        ResultSet foreignKeys = md.getImportedKeys(catalog, schema, table);
        while (foreignKeys.next()) {
            String fkTableName = foreignKeys.getString("FKTABLE_NAME");
            String fkColumnName = foreignKeys.getString("FKCOLUMN_NAME");
            String pkTableName = foreignKeys.getString("PKTABLE_NAME");
            String pkColumnName = foreignKeys.getString("PKCOLUMN_NAME");

            fk.addDependency(fkTableName, fkColumnName, pkTableName, pkColumnName);
        }
    }

    private static void getData(MConnection con, Table table, String db){
        try {
            String selectAll = String.format("SELECT * FROM `%s`.`%s`", db, table);
            try (PreparedStatement selectStmt = con.getConnection().prepareStatement(selectAll, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY); ResultSet rs = selectStmt.executeQuery()) {
                if (!rs.isBeforeFirst()) {
                    System.out.println("No rows found");
                } else {
                    String createQuery = null;
                    while (rs.next()) {
                        if (createQuery == null) {
                            createQuery = buildCreateTableStatement(table, rs);
                            System.out.println(createQuery);
                        }

                        buildInsertStatement(table, db, rs);
                    }
                }
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }

        //throw new RuntimeException("Stopping");
    }

    public static String buildInsertStatement(Table table, String db, ResultSet rs) throws SQLException {
        StringBuilder sb = new StringBuilder();

        ResultSetMetaData metaData = rs.getMetaData();
        int colCount = metaData.getColumnCount() + 1;

        sb.append(String.format("INSERT INTO `%s`.`%s` VALUES (", db, table.getName()));
        String concat = "";
        for (int i = 1; i < colCount; i++) {
            String scrambleValue;
            String columnName = metaData.getColumnName(i);

            //TODO if link
            scrambleValue = scrambleValue(metaData.getColumnClassName(i), metaData.getPrecision(i));


            String actualValue = rs.getObject(i).toString();

            sb.append(concat);
            sb.append(scrambleValue);
            concat = ",";
        }
        sb.append(");");

        return sb.toString();
    }

    private static String buildCreateTableStatement(Table table, ResultSet rs) throws SQLException {
        StringBuilder sb = new StringBuilder();
        ResultSetMetaData metaData = rs.getMetaData();
        int colCount = metaData.getColumnCount() + 1;

        Boolean noKeys = table.getPks().isEmpty();
        sb.append(String.format("CREATE TABLE `%s` (", table.getName()));
        for (int i = 1; i < colCount; i++) {
            String label = metaData.getColumnName(i);
            String type = metaData.getColumnTypeName(i);
            String length = Integer.toString(metaData.getPrecision(i));
            sb.append(String.format("`%s` %s(%s)", label, type, length));

            if (!(noKeys && i + 1 == colCount)) {
                sb.append(",");
            }
        }

        if (!noKeys) {
            String primKey = buildPrimaryKeys(table.getPks());
            sb.append(String.format("PRIMARY KEY (`%s`)", primKey));
        }

        sb.append(String.format(") ENGINE=%s DEFAULT CHARSET=%s COLLATE=%s;", ENGINE, CHARSET, COLLATE));
        return sb.toString();
    }

    private static String buildPrimaryKeys(List<String> keys){
        if(keys.size() == 1) {
            return keys.get(0);
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