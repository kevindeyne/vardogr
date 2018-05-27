package com.kevindeyne.datascrambler.helper;

import com.kevindeyne.datascrambler.domain.Config;
import com.kevindeyne.datascrambler.domain.MConnection;
import org.springframework.util.CollectionUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.kevindeyne.datascrambler.helper.Scrambler.scrambleValue;

public class Copying {
    //TODO move to config?
    public static String CHARSET = "utf8";
    public static String ENGINE = "InnoDB";
    public static String COLLATE = "utf8_bin";

    private static Map<String, Map<String, String>> keyCoupling = new HashMap<>();

    public static void downloadDatabase(Config obj, String db) {
        try {
            MConnection con = obj.buildConnection(db);
            DatabaseMetaData md = con.getConnection().getMetaData();
            ResultSet rs = getAllTables(md);

            while (rs.next()) {
                String table = getTableName(rs);
                List<String> keys = getKeys(md, table);

                System.out.println("Downloading: " + table);

                getData(con, table, db, keys);

            }

        } catch(SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static ResultSet getAllTables(DatabaseMetaData md) throws SQLException {
        return md.getTables(null, null, "%", null);
    }

    private static String getTableName(ResultSet rs) throws SQLException {
        String table = rs.getString(3);
        keyCoupling.put(table, new HashMap<>());
        return table;
    }

    private static List<String> getKeys(DatabaseMetaData md, String table) throws SQLException {
        List<String> primKeys = new ArrayList<>();
        ResultSet rs = md.getPrimaryKeys( "" , "" , table );
        while(rs.next()) {
            ResultSetMetaData metaData = rs.getMetaData();

            for (int i = 1; i < metaData.getColumnCount() + 1; i++) {
                String colName = metaData.getColumnName(i);
                if(colName.contains("COL")){
                    primKeys.add(rs.getObject(i).toString());
                }
            }
        }
        return primKeys;
    }

    private static void printForeignKeys(Connection connection, String tableName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet foreignKeys = metaData.getImportedKeys(connection.getCatalog(), null, tableName);
        while (foreignKeys.next()) {
            String fkTableName = foreignKeys.getString("FKTABLE_NAME");
            String fkColumnName = foreignKeys.getString("FKCOLUMN_NAME");
            String pkTableName = foreignKeys.getString("PKTABLE_NAME");
            String pkColumnName = foreignKeys.getString("PKCOLUMN_NAME");
            System.out.println(fkTableName + "." + fkColumnName + " -> " + pkTableName + "." + pkColumnName);
        }
    }

    private static void getData(MConnection con, String table, String db, List<String> keys){
        try {
            boolean noKeys = CollectionUtils.isEmpty(keys);

            String selectAll = String.format("SELECT * FROM `%s`.`%s`", db, table);
            try (PreparedStatement selectStmt = con.getConnection().prepareStatement(selectAll, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY); ResultSet rs = selectStmt.executeQuery()) {
                if (!rs.isBeforeFirst()) {
                    System.out.println("No rows found");
                } else {
                    String createQuery = null;
                    while (rs.next()) {
                        if (createQuery == null) {
                            createQuery = buildCreateTableStatement(table, keys, noKeys, rs);
                            System.out.println(createQuery);
                        }

                        buildInsertStatement(table, db, rs, keys);
                    }
                }
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }

        //throw new RuntimeException("Stopping");
    }

    private static void buildInsertStatement(String table, String db, ResultSet rs, List<String> keys) throws SQLException {
        StringBuilder sb = new StringBuilder();

        ResultSetMetaData metaData = rs.getMetaData();
        int colCount = metaData.getColumnCount() + 1;

        sb.append(String.format("INSERT INTO `%s`.`%s` VALUES (", db, table));
        String concat = "";
        for (int i = 1; i < colCount; i++) {
            String scrambleValue = scrambleValue(metaData.getColumnClassName(i), metaData.getPrecision(i));

            /*if(keys.contains(metaData.getColumnName(i))){
                keyCoupling.get(table).put(rs.getObject(i).toString(), scrambleValue);
            }*/

            sb.append(concat);

            sb.append(scrambleValue);
            concat = ",";
        }
        sb.append(");");

        System.out.println(sb.toString());
    }

    private static String buildCreateTableStatement(String table, List<String> keys, boolean noKeys, ResultSet rs) throws SQLException {
        StringBuilder sb = new StringBuilder();
        ResultSetMetaData metaData = rs.getMetaData();
        int colCount = metaData.getColumnCount() + 1;

        sb.append(String.format("CREATE TABLE `%s` (", table));
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
            String primKey = buildPrimaryKeys(keys);
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