package com.kevindeyne.datascrambler.domain;

import com.google.gson.JsonObject;
import com.kevindeyne.datascrambler.dao.SourceConnectionDao;
import com.kevindeyne.datascrambler.dao.TargetConnectionDao;
import com.kevindeyne.datascrambler.exceptions.ConnectionFailureException;
import com.kevindeyne.datascrambler.helper.SupportedDBType;
import com.kevindeyne.datascrambler.service.EncryptService;
import com.kevindeyne.datascrambler.service.GenerationService;
import lombok.Data;

import java.sql.SQLException;

@Data
public class Config {

    private String usernameSource;
    private String passwordSource;
    private Integer portSource;
    private String hostSource;
    private String dbNameSource;
    private String schemaSource;
    private SupportedDBType dbTypeSource;

    private String usernameTarget;
    private String passwordTarget;
    private Integer portTarget;
    private String hostTarget;
    private String dbNameTarget;
    private SupportedDBType dbTypeTarget;

    public static final String HOST_SOURCE = "host-source";
    public static final String PORT_SOURCE = "port-source";
    public static final String USERNAME_SOURCE = "username-source";
    public static final String PASSWORD_SOURCE = "password-source";
    public static final String DB_NAME_SOURCE = "dbName-source";
    public static final String SCHEMA_SOURCE = "schema-source";
    public static final String DB_TYPE_SOURCE = "dbType-source";
    public static final String HOST_TARGET = "host-target";

    public static final String PORT_TARGET = "port-target";
    public static final String USERNAME_TARGET = "username-target";
    public static final String PASSWORD_TARGET = "password-target";
    public static final String DB_NAME_TARGET = "dbName-target";
    public static final String DB_TYPE_TARGET = "dbType-target";
    public static final String EMPTY = "^";

    public Config(final JsonObject obj, final EncryptService encryptService) {
        hostSource = obj.get(HOST_SOURCE).getAsString();
        usernameSource = obj.get(USERNAME_SOURCE).getAsString();
        passwordSource = encryptService.decrypt(obj.get(PASSWORD_SOURCE).getAsString());
        portSource = obj.get(PORT_SOURCE).getAsInt();
        dbNameSource = obj.get(DB_NAME_SOURCE).getAsString();
        schemaSource = obj.get(SCHEMA_SOURCE).getAsString();
        dbTypeSource = SupportedDBType.valueOf(obj.get(DB_TYPE_SOURCE).getAsString().toUpperCase());

        if(!EMPTY.equals(obj.get(HOST_TARGET).getAsString())) {
            hostTarget = obj.get(HOST_TARGET).getAsString();
            usernameTarget = obj.get(USERNAME_TARGET).getAsString();
            passwordTarget = encryptService.decrypt(obj.get(PASSWORD_TARGET).getAsString());
            portTarget = obj.get(PORT_TARGET).getAsInt();
            dbNameTarget = obj.get(DB_NAME_TARGET).getAsString();
            dbTypeTarget = SupportedDBType.valueOf(obj.get(DB_TYPE_TARGET).getAsString().toUpperCase());
        }
    }

    public SourceConnectionDao setupSourceConnection() throws ConnectionFailureException {
        String url = setupUrl(dbTypeSource.getPlaceholder(), hostSource, portSource, dbNameSource);
        SourceConnectionDao connection = new SourceConnectionDao(url, usernameSource, passwordSource, dbTypeSource.getSQLDialect());
        try {
            if (connection.testConnection()) return connection;
        } catch(SQLException e) {
            throw new ConnectionFailureException("Could not connect to SOURCE DB");
        }
        throw new ConnectionFailureException("Could not connect to SOURCE DB");
    }

    public TargetConnectionDao setupTargetConnection(GenerationService generationService) throws ConnectionFailureException {
        String url = setupUrl(dbTypeTarget.getPlaceholder(), hostTarget, portTarget, dbNameTarget);
        TargetConnectionDao connection = new TargetConnectionDao(url, usernameTarget, passwordTarget, generationService);
        try {
            if (connection.testConnection()) return connection;
        } catch(SQLException e) {
            e.printStackTrace();
            throw new ConnectionFailureException("Could not connect to TARGET DB");
        }
        throw new ConnectionFailureException("Could not connect to TARGET DB");
    }

    private String setupUrl(String urlPlaceholder, String host, Integer port, String dbName) {
        return urlPlaceholder.replace("host", host).replace("port", port.toString()).replace("dbname", dbName);
    }
}
