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

    public MConnection buildConnection(String db){
        Properties connectionProps = new Properties();
        connectionProps.put("user", getUsername());
        connectionProps.put("password", getPassword());

        try {
            if(null == db){ db = ""; }
            String url = "jdbc:" + type + "://" + host + ":" + port + "/" + db + "?useSSL=false";
            Connection con = DriverManager.getConnection(url, connectionProps);
            return new MConnection(con, db);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public MConnection buildConnection(){
        return buildConnection(null);
    }

    public String getHost() {
        return host;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getType() {
        return type;
    }

    public int getPort() {
        return port;
    }

    public void printProps() {
        System.out.println();
        System.out.println("Config: ");
        System.out.println(" Host: " + getHost());
        System.out.println(" Port: " + getPort());
        System.out.println(" Username: " + getUsername());
        System.out.println();
    }
}
