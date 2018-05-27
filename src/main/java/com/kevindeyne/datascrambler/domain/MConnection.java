package com.kevindeyne.datascrambler.domain;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MConnection {

    Connection connection;

    public MConnection(Connection connection) {
        this.connection = connection;
    }

    public String getFoundDatabases() {
        try{
            ResultSet rs = connection.getMetaData().getCatalogs();

            StringBuilder sb = new StringBuilder();
            String concat = "";
            while (rs.next()) {
                sb.append(concat);
                sb.append(rs.getString("TABLE_CAT"));
                concat = ", ";
            }
            return sb.toString();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
