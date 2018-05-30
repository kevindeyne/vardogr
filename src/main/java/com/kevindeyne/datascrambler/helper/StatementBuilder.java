package com.kevindeyne.datascrambler.helper;

import com.kevindeyne.datascrambler.domain.ForeignKey;
import com.kevindeyne.datascrambler.domain.Table;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class StatementBuilder {

    //TODO move to config?
    private static String CHARSET = "utf8";
    private static String ENGINE = "InnoDB";
    private static String COLLATE = "utf8_bin";

    public static String buildInsertStatement(Table table, String db, ResultSet rs) throws SQLException {
        StringBuilder sb = new StringBuilder();

        ResultSetMetaData metaData = rs.getMetaData();
        int colCount = metaData.getColumnCount() + 1;

        sb.append(String.format("INSERT INTO `%s`.`%s` VALUES (", db, table.getName()));
        String concat = "";
        for (int i = 1; i < colCount; i++) {
            String scrambleValue = null;
            String columnName = metaData.getColumnName(i);

            if(table.getPks().contains(columnName)){
                scrambleValue = Scrambler.consistentValue(metaData.getColumnClassName(i), rs.getRow(), Long.MAX_VALUE, Long.MAX_VALUE, false);
            }

            for(ForeignKey fk : table.getFks()){
                if(fk.getVia().equals(columnName)){
                    Boolean nullable = metaData.isNullable(i) == ResultSetMetaData.columnNullable;
                    scrambleValue = Scrambler.consistentValue(metaData.getColumnClassName(i), rs.getRow(), fk.getFkCount(), fk.getTableSize(), nullable);
                    break;
                }
            }

            if(scrambleValue == null) {
                scrambleValue = Scrambler.scrambleValue(columnName, metaData.getColumnClassName(i), metaData.getPrecision(i));
            }

            sb.append(concat);
            sb.append(scrambleValue);
            concat = ",";
        }
        sb.append(");");

        return sb.toString();
    }

    public static String buildCreateTableStatement(Table table, ResultSet rs, String db) throws SQLException {
        StringBuilder sb = new StringBuilder();
        ResultSetMetaData metaData = rs.getMetaData();
        int colCount = metaData.getColumnCount() + 1;

        Boolean noKeys = table.getPks().isEmpty();
        sb.append(String.format("CREATE TABLE `%s`.`%s` (", db, table.getName()));
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
            String primKey = KeyBuilder.buildPrimaryKeys(table.getPks());
            sb.append(String.format("PRIMARY KEY (`%s`)", primKey));
        }

        sb.append(String.format(") ENGINE=%s DEFAULT CHARSET=%s COLLATE=%s;", ENGINE, CHARSET, COLLATE));
        return sb.toString();
    }

    public static String buildFKStatement(Table table, ForeignKey key, String db) {
        String refPrimKey = KeyBuilder.buildPrimaryKeys(key.getTable().getPks());
        return String.format("ALTER TABLE `%s`.`%s` ADD FOREIGN KEY (`%s`) REFERENCES `%s`(`%s`);", db, table.getName(), key.getVia(), key.getTable().getName(), refPrimKey);
    }
}
