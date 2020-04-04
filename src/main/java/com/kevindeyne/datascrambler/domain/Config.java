package com.kevindeyne.datascrambler.domain;

import com.grack.nanojson.JsonObject;
import com.kevindeyne.datascrambler.exceptions.ConnectionFailureException;
import com.kevindeyne.datascrambler.helper.SupportedDBType;
import com.kevindeyne.datascrambler.service.EncryptService;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Data;

import java.sql.SQLException;

@Data
public class Config {

    private String username;
    private String password;
    private Integer port;
    private String host;
    private String dbName;
    private SupportedDBType dbType;

    public Config(final JsonObject obj, final EncryptService encryptService) {
        host = obj.getString("host");
        username = obj.getString("username");
        password = encryptService.decrypt(obj.getString("password"));
        port = obj.getInt("port");
        dbName = obj.getString("dbName");
        dbType = SupportedDBType.valueOf(obj.getString("dbType").toUpperCase());
    }

    public ProdConnection setupProdConnection() throws ConnectionFailureException {
        String url = setupUrl(dbType.getPlaceholder(), host, port, dbName);
        ProdConnection connection = new ProdConnection(url, username, password);
        try {
            if (connection.testConnection()) return connection;
        } catch(SQLException e) {
            throw new ConnectionFailureException("Could not connect to PROD DB");
        }
        throw new ConnectionFailureException("Could not connect to PROD DB");
    }

    private String setupUrl(String urlPlaceholder, String host, Integer port, String dbName) {
        return urlPlaceholder.replace("host", host).replace("port", port.toString()).replace("dbname", dbName);
    }
}
