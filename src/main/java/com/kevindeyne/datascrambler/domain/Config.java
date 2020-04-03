package com.kevindeyne.datascrambler.domain;

import com.grack.nanojson.JsonObject;
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
        host = obj.getString("host");
        username = obj.getString("username");
        password = encryptService.decrypt(obj.getString("password"));
        port = obj.getInt("port");
        dbName = obj.getString("dbName");
        dbType = SupportedDBType.valueOf(obj.getString("dbType").toUpperCase());
    }

    public ProdConnection setupProdConnection() throws SQLException {
        ProdConnection connection = new ProdConnection();

        connection.setupConnection(setupUrl(dbType.getPlaceholder(), host, port, dbName), username, password);
        if (connection.testConnection()) return connection;

        throw new RuntimeException("Could not connect to PROD DB");
    }

    private String setupUrl(String urlPlaceholder, String host, Integer port, String dbName) {
        return urlPlaceholder.replace("host", host).replace("port", port.toString()).replace("dbname", dbName);
    }
}
