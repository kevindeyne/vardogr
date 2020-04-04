package com.kevindeyne.datascrambler.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.kevindeyne.datascrambler.domain.Config;
import com.kevindeyne.datascrambler.exceptions.ConfigFileException;
import com.kevindeyne.datascrambler.helper.SupportedDBType;
import com.kevindeyne.datascrambler.shell.InputReader;
import com.kevindeyne.datascrambler.shell.ShellHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class ConfigService {

    @Lazy
    @Autowired
    private ShellHelper shellHelper;

    @Lazy
    @Autowired
    private InputReader input;

    @Autowired
    private FileService fileService;

    @Autowired
    private EncryptService encryptService;

    public static final String CONFIG_JSON = "config.json";

    public Config loadConfig() {
        return loadConfig(false);
    }

    public Config loadConfig(boolean force) {
        final JsonObject configObj;
        try {
            if (!force && fileService.doesFileExist(CONFIG_JSON)) {
                configObj = readConfigObj();
            } else {
                configObj = new JsonObject();
                configObj.addProperty("host", input.getString("Host", "localhost"));
                configObj.addProperty("port", input.getInteger("Port", 3306));
                configObj.addProperty("username", input.getString("Username"));
                configObj.addProperty("password", encryptService.encrypt(input.getPassword("Password")));
                configObj.addProperty("dbName", input.getString("Database name"));
                configObj.addProperty("dbType", input.getOption("DB type", SupportedDBType.all()));
                fileService.writeToFile(configObj, CONFIG_JSON);
            }
        } catch (Exception e) {
            shellHelper.printError("Config file corrupt. Restarting file creation ...");
            fileService.deleteFile(CONFIG_JSON);
            return loadConfig(true);
        }

        try {
            return new Config(configObj, encryptService);
        } catch (Exception e) {
            shellHelper.printError("Config file has corrupted content. Restarting file creation ...");
            fileService.deleteFile(CONFIG_JSON);
            return loadConfig(true);
        }
    }

    private JsonObject readConfigObj() throws ConfigFileException {
        String fileContents = fileService.loadFile(CONFIG_JSON);
        try {
            return new Gson().fromJson(fileContents, JsonObject.class);
        } catch (Exception e) {
            throw new ConfigFileException("Could not read config file: " + e.getMessage(), e);
        }
    }
}
