package com.kevindeyne.datascrambler.service;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
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
                configObj.put("host", input.getString("Host", "localhost"));
                configObj.put("port", input.getInteger("Port", 3306));
                configObj.put("username", input.getString("Username"));
                configObj.put("password", encryptService.encrypt(input.getPassword("Password")));
                configObj.put("dbName", input.getString("Database name"));
                configObj.put("dbType", input.getOption("DB type", SupportedDBType.all()));
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
            return JsonParser.object().from(fileContents);
        } catch (JsonParserException e) {
            throw new ConfigFileException("Could not read config file: " + e.getMessage(), e);
        }
    }
}
