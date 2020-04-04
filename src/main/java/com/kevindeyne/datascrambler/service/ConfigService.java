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

import static com.kevindeyne.datascrambler.domain.Config.*;
import static com.kevindeyne.datascrambler.domain.MessageConstants.*;

@Service
public class ConfigService {

    @Lazy
    @Autowired
    private InputReader input;

    @Autowired
    private ShellHelper shellHelper;

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
            if (!force && fileService.doesFileExist(CONFIG_JSON, MSG_CONFIG_FOUND, MSG_CONFIG_NOT_FOUND)) {
                configObj = readConfigObj();
            } else {
                configObj = new JsonObject();
                configObj.addProperty(HOST_SOURCE, input.getString("Hostname (Source)", "localhost"));
                configObj.addProperty(PORT_SOURCE, input.getInteger("Port (Source)", 3306));
                configObj.addProperty(USERNAME_SOURCE, input.getString("Username (Source)"));
                configObj.addProperty(PASSWORD_SOURCE, encryptService.encrypt(input.getPassword("Password (Source)")));
                configObj.addProperty(DB_NAME_SOURCE, input.getString("Database name (Source)"));
                configObj.addProperty(DB_TYPE_SOURCE, input.getOption("DB type (Source)", SupportedDBType.all()));

                configObj.addProperty(HOST_TARGET, EMPTY);
                configObj.addProperty(PORT_TARGET, 0);
                configObj.addProperty(USERNAME_TARGET, EMPTY);
                configObj.addProperty(PASSWORD_TARGET, EMPTY);
                configObj.addProperty(DB_NAME_TARGET, EMPTY);
                configObj.addProperty(DB_TYPE_TARGET, EMPTY);
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
