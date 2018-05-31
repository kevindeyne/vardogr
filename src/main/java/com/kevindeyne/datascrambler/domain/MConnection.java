package com.kevindeyne.datascrambler.domain;

import java.sql.*;

public class MConnection {

    private Connection connection;
    private String catalog;
    private String schema;
    private String db;

    private static final String TABLE_CAT = "TABLE_CAT";
    private static final String COUNT = "SELECT count(*) FROM `%s`.`%s`";

    public MConnection(Connection connection, String databaseName) {
        this.connection = connection;
        this.db = databaseName;

        try {
            this.catalog = connection.getCatalog();
            this.schema = connection.getSchema();
        } catch (SQLException e){
            //keep null
        }
    }

    public String getFoundDatabases() {
        try{
            ResultSet rs = connection.getMetaData().getCatalogs();

            StringBuilder sb = new StringBuilder();
            String concat = "";
            while (rs.next()) {
                sb.append(concat);
                sb.append(rs.getString(TABLE_CAT));
                concat = ", ";
            }
            return sb.toString();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ResultSet getPrimaryKeys(String table) throws SQLException {
        DatabaseMetaData md = getConnection().getMetaData();
        return md.getPrimaryKeys(getCatalog(), getSchema(), table);
    }

    public Long countTableSize(String tableName) {
        try (PreparedStatement s = prepare(MConnection.COUNT, getDb(), tableName); ResultSet rs = s.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return -1L;
    }

    private PreparedStatement prepare(String sql, String ... parameters) throws SQLException {
        return getConnection()
                .prepareStatement(String.format(sql, parameters),
                        ResultSet.TYPE_SCROLL_INSENSITIVE,
                        ResultSet.CONCUR_READ_ONLY);
    }

    public Connection getConnection() {
        return connection;
    }

    public String getCatalog() {
        return catalog;
    }

    public String getSchema() {
        return schema;
    }

    public String getDb() {
        return db;
    }
}
