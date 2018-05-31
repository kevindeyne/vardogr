package com.kevindeyne.datascrambler.helper;

import com.kevindeyne.datascrambler.domain.Column;
import com.kevindeyne.datascrambler.domain.ForeignKey;
import com.kevindeyne.datascrambler.domain.Table;

import java.math.BigDecimal;
import java.sql.*;

public class StatementBuilder {

    public static final String EMPTY = "";
    public static final String COMMA = ",";

    private static final String ALLOWS_NULL = "NULLABLE";
    private static final String DATA_TYPE = "DATA_TYPE";
    private static final String COLUMN_NAME = "COLUMN_NAME";
    private static final String COLUMN_SIZE = "COLUMN_SIZE";
    private static final String TYPE_NAME = "TYPE_NAME";
    private static final String CLOSE_STATEMENT = ");";

    //TODO move to config?
    private static String CHARSET = "utf8";
    private static String ENGINE = "InnoDB";
    private static String COLLATE = "utf8_bin";

    public static String buildInsertStatement(Table table, String db, int i) {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("INSERT INTO `%s`.`%s` VALUES (", db, table.getName()));
        String concat = EMPTY;
        for(Column col : table.getColumns()) {
            String scrambleValue = null;

            if(table.getPks().contains(col.getName())){
                scrambleValue = Scrambler.consistentValue(col.getClazz(), i, Long.MAX_VALUE, false);
            }

            for(ForeignKey fk : table.getFks()){
                if(fk.getVia().equals(col.getName())){
                    scrambleValue = Scrambler.consistentValue(col.getClazz(), i, fk.getTable().getTableSize(), col.isNullable());
                    break;
                }
            }

            if(scrambleValue == null) {
                scrambleValue = Scrambler.scrambleValue(col.getClazz(), col.getName(), col.getPrecision());
            }

            sb.append(concat);
            sb.append(scrambleValue);
            concat = COMMA;
        }

        sb.append(CLOSE_STATEMENT);

        return sb.toString();
    }

    public static String buildCreateTableStatement(Table table, ResultSet rs, String db) throws SQLException, ClassNotFoundException {
        StringBuilder sb = new StringBuilder();

        Boolean noKeys = table.getPks().isEmpty();
        table.resetColumns();
        sb.append(String.format("CREATE TABLE `%s`.`%s` (", db, table.getName()));

        while (rs.next()) {
            String label = rs.getString(COLUMN_NAME);
            Class clazz = toJavaType(rs.getInt(DATA_TYPE));
            String type = rs.getString(TYPE_NAME);
            int length = rs.getInt(COLUMN_SIZE);
            boolean allowsNulls = rs.getBoolean(ALLOWS_NULL);

            table.getColumns().add(new Column(label, clazz, length, allowsNulls));
            sb.append(String.format("`%s` %s(%s)", label, type, Integer.toString(length)));

            if (!rs.isLast()) {
                sb.append(",");
            }
        }

        if (!noKeys) {
            String primKey = KeyBuilder.buildPrimaryKeys(table.getPks());
            sb.append(String.format(" PRIMARY KEY (`%s`)", primKey));
        }

        sb.append(");");
        //sb.append(String.format(") ENGINE=%s DEFAULT CHARSET=%s COLLATE=%s;", ENGINE, CHARSET, COLLATE));
        return sb.toString();
    }

    public static String buildFKStatement(Table table, ForeignKey key, String db) {
        String refPrimKey = KeyBuilder.buildPrimaryKeys(key.getTable().getPks());
        return String.format("ALTER TABLE `%s`.`%s` ADD FOREIGN KEY (`%s`) REFERENCES `%s`(`%s`);", db, table.getName(), key.getVia(), key.getTable().getName(), refPrimKey);
    }

    public static Class toJavaType(int sqlType) {
        if (sqlType == Types.INTEGER) {
            return Integer.class;
        } else if (sqlType == Types.VARCHAR) {
            return String.class;
        } else if (sqlType == Types.CHAR) {
            return String.class;
        } else if (sqlType == Types.LONGVARCHAR) {
            return String.class;
        } else if (sqlType == Types.NVARCHAR) {
            return String.class;
        } else if (sqlType == Types.NCHAR) {
            return String.class;
        } else if (sqlType == Types.LONGNVARCHAR) {
            return String.class;
        } else if (sqlType == Types.NUMERIC) {
            return BigDecimal.class;
        } else if (sqlType == Types.DECIMAL) {
            return BigDecimal.class;
        } else if (sqlType == Types.BIT) {
            return Boolean.class;
        } else if (sqlType == Types.BOOLEAN) {
            return Boolean.class;
        } else if (sqlType == Types.TINYINT) {
            return Integer.class;
        } else if (sqlType == Types.SMALLINT) {
            return Integer.class;
        } else if (sqlType == Types.BIGINT) {
            return Long.class;
        } else if (sqlType == Types.REAL) {
            return Float.class;
        } else if (sqlType == Types.FLOAT) {
            return Double.class;
        } else if (sqlType == Types.DOUBLE) {
            return Double.class;
        } else if (sqlType == Types.DATE) {
            return Date.class;
        } else if (sqlType == Types.TIME) {
            return Time.class;
        } else if (sqlType == Types.TIMESTAMP) {
            return Timestamp.class;
        } else {
            return null;
        }
    }
}