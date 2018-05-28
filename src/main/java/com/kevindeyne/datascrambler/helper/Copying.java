package com.kevindeyne.datascrambler.helper;

import com.kevindeyne.datascrambler.domain.Config;
import com.kevindeyne.datascrambler.domain.Dependency;
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
    static String CHARSET = "utf8";
    static String ENGINE = "InnoDB";
    static String COLLATE = "utf8_bin";

    private static FKMapping fk = new FKMapping();

    public static void downloadDatabase(Config obj, String db) {
        try {
            MConnection con = obj.buildConnection(db);
            Connection connection = con.getConnection();
            DatabaseMetaData md = connection.getMetaData();
            String catalog = connection.getCatalog();
            String schema = connection.getSchema();

            ResultSet rs = getAllTables(md, catalog, schema);
            while (rs.next()) {
                String tableName = getTableName(rs);
                fk.keys.put(tableName, Copying.getKeys(md, tableName, catalog, schema));
            }

            fk.handleDependenciesWithNoChildren(con, db);
        } catch(SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static ResultSet getAllTables(DatabaseMetaData md, String catalog, String schema) throws SQLException {
        return md.getTables(null, null, "%", null);
    }

    private static String getTableName(ResultSet rs) throws SQLException {
        String table = rs.getString(3);
        return table;
    }

    protected static List<String> getKeys(DatabaseMetaData md, String table, String catalog, String schema) throws SQLException {
        List<String> primKeys = new ArrayList<>();
        ResultSet primaryKeys = md.getPrimaryKeys( catalog , schema , table );
        while(primaryKeys.next()) {
            ResultSetMetaData metaData = primaryKeys.getMetaData();

            for (int i = 1; i < metaData.getColumnCount() + 1; i++) {
                String colName = metaData.getColumnName(i);
                if(colName.contains("COL")){
                    String primKeyColName = primaryKeys.getObject(i).toString();
                    primKeys.add(primKeyColName);
                    fk.addTable(table, primKeyColName);
                }
            }
        }

        loadFKs(md, table, catalog, schema);

        return primKeys;
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

    protected static void getData(MConnection con, String table, String db, List<String> keys){
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
        Dependency fkData = fk.dependencyMap.get(table);

        StringBuilder sb = new StringBuilder();

        ResultSetMetaData metaData = rs.getMetaData();
        int colCount = metaData.getColumnCount() + 1;

        sb.append(String.format("INSERT INTO `%s`.`%s` VALUES (", db, table));
        String concat = "";
        for (int i = 1; i < colCount; i++) {
            String scrambleValue;
            String actualValue = rs.getObject(i).toString();
            String columnName = metaData.getColumnName(i);
            String currentJoined = String.format("%s.%s", table, columnName);

            if(fk.dValueMap.get(currentJoined) != null){
                scrambleValue = fk.dValueMap.get(currentJoined).get(actualValue);
                //System.out.println(String.format("# %s exists; for col %s; value is %s", currentJoined, columnName, scrambleValue));
            } else {
                scrambleValue = scrambleValue(metaData.getColumnClassName(i), metaData.getPrecision(i));
            }

            //add child values if required
            if(fkData != null && fkData.getColumn().equals(columnName)) {
                for(Dependency parent : fkData.getParents()){
                     String joined = String.format("%s.%s", parent.getTable(), parent.getColumn());

                    if(fk.dValueMap.get(joined) != null){
                        fk.dValueMap.get(joined).put(actualValue, scrambleValue);
                    } else {
                        Map<String, String> valueMap = new HashMap<>();
                        valueMap.put(actualValue, scrambleValue);
                        fk.dValueMap.put(joined, valueMap);
                    }
                }
            }
            //---

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
            String primKey = buildPrimaryKeys(table, keys);
            sb.append(String.format("PRIMARY KEY (`%s`)", primKey));
        }

        sb.append(String.format(") ENGINE=%s DEFAULT CHARSET=%s COLLATE=%s;", ENGINE, CHARSET, COLLATE));
        return sb.toString();
    }

    private static String buildPrimaryKeys(String table, List<String> keys){
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