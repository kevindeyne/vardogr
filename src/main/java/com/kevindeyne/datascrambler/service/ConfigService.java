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

    public Config loadTargetConfig() {
        return loadTargetConfig(false);
    }

    public Config loadTargetConfig(boolean force) {
        final JsonObject configObj;

        try {
            if (!force && fileService.doesFileExist(CONFIG_JSON, MSG_CONFIG_FOUND, MSG_CONFIG_NOT_FOUND)) {
                configObj = readConfigObj();

                if(EMPTY.equals(configObj.get(HOST_TARGET).getAsString())) {
                    configObj.addProperty(HOST_TARGET, input.getString("Hostname (Target)", "localhost"));
                    configObj.addProperty(PORT_TARGET, input.getInteger("Port (Target)", 3306));
                    configObj.addProperty(USERNAME_TARGET, input.getString("Username (Target)"));
                    configObj.addProperty(PASSWORD_TARGET, encryptService.encrypt(input.getPassword("Password (Target)")));
                    configObj.addProperty(DB_NAME_TARGET, input.getString("Database name (Target)"));
                    configObj.addProperty(DB_TYPE_TARGET, input.getOption("DB type (Target)", SupportedDBType.all()));
                    fileService.writeToFile(configObj, CONFIG_JSON);
                }
            } else {
                throw new RuntimeException("Could not read config file");
            }
        } catch (Exception e) {
            e.printStackTrace();
            shellHelper.printError("Config file corrupt. Restarting file creation ...");
            throw new RuntimeException(e);
        }

        try {
            return new Config(configObj, encryptService);
        } catch (Exception e) {
            e.printStackTrace();
            fileService.deleteFile(CONFIG_JSON);
            throw new RuntimeException(e);
        }
    }


    public Config loadSourceConfig() {
        return loadSourceConfig(false);
    }

    public boolean hasConfig() {
        return fileService.doesFileExist(CONFIG_JSON, MSG_CONFIG_FOUND, "No config file yet defined. Run 'build' for the wizard to get your started.");
    }

    public Config loadSourceConfig(boolean force) {
        final JsonObject configObj;

        try {
            if (!force && fileService.doesFileExist(CONFIG_JSON, MSG_CONFIG_FOUND, MSG_CONFIG_NOT_FOUND)) {
                configObj = readConfigObj();
            } else {
                configObj = new JsonObject();
                configObj.addProperty(HOST_SOURCE, input.getString("[1/7] > Hostname (Source)", "localhost"));
                configObj.addProperty(PORT_SOURCE, input.getInteger("[2/7] > Port (Source)", 3306));
                configObj.addProperty(USERNAME_SOURCE, input.getString("[3/7] > Username (Source)"));
                configObj.addProperty(PASSWORD_SOURCE, encryptService.encrypt(input.getPassword("[4/7] > Password (Source)")));
                configObj.addProperty(DB_NAME_SOURCE, input.getString("[5/7] > Database name (Source)"));
                configObj.addProperty(SCHEMA_SOURCE, input.getString("[6/7] > Schema name (Source, Optional)", ""));
                configObj.addProperty(DB_TYPE_SOURCE, input.getOption("[7/7] > DB type (Source)", SupportedDBType.all()));

                configObj.addProperty(HOST_TARGET, EMPTY);
                configObj.addProperty(PORT_TARGET, 0);
                configObj.addProperty(USERNAME_TARGET, EMPTY);
                configObj.addProperty(PASSWORD_TARGET, EMPTY);
                configObj.addProperty(DB_NAME_TARGET, EMPTY);
                configObj.addProperty(DB_TYPE_TARGET, EMPTY);
                fileService.writeToFile(configObj, CONFIG_JSON);
            }
        } catch (Exception e) {
            shellHelper.printError("Config file corrupt.");
            fileService.deleteFile(CONFIG_JSON);
            throw new RuntimeException(e);
        }

        try {
            return new Config(configObj, encryptService);
        } catch (Exception e) {
            shellHelper.printError("Config file has corrupted content.");
            fileService.deleteFile(CONFIG_JSON);
            throw e;
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

    public void clearConfigs() {
        fileService.deleteFile(CONFIG_JSON);
    }
}
