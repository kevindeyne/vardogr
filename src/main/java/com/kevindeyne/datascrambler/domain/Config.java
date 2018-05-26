package com.kevindeyne.datascrambler.domain;

import com.grack.nanojson.JsonObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Config {

    private String host;
    private String username;
    private String password;
    private String type;
    private int port;

    public Config(JsonObject obj) {
        host = obj.getString("host");
        username = obj.getString("username");
        password = obj.getString("password");
        type = obj.getString("type"); //TODO validate
        port = obj.getInt("port");
    }

    public MConnection buildConnection(){
        Properties connectionProps = new Properties();
        connectionProps.put("user", getUsername());
        connectionProps.put("password", getPassword());

        try {
            Connection con = DriverManager.getConnection("jdbc:" + type + "://" + host + ":" + port + "/?useSSL=false", connectionProps);
            return new MConnection(con);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void printProps() {
        System.out.println("   Host: " + getHost());
        System.out.println("   Port: " + getPort());
        System.out.println("   Username: " + getUsername());
    }
}
