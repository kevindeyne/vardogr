package com.kevindeyne.datascrambler.domain;

import com.google.gson.JsonObject;
import com.kevindeyne.datascrambler.exceptions.ConnectionFailureException;
import com.kevindeyne.datascrambler.helper.SupportedDBType;
import com.kevindeyne.datascrambler.service.EncryptService;
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
        host = obj.get("host").getAsString();
        username = obj.get("username").getAsString();
        password = encryptService.decrypt(obj.get("password").getAsString());
        port = obj.get("port").getAsInt();
        dbName = obj.get("dbName").getAsString();
        dbType = SupportedDBType.valueOf(obj.get("dbType").getAsString().toUpperCase());
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
