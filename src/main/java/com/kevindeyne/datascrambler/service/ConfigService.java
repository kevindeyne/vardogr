package com.kevindeyne.datascrambler.service;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import com.grack.nanojson.JsonWriter;
import com.kevindeyne.datascrambler.domain.Config;
import com.kevindeyne.datascrambler.shell.InputReader;
import com.kevindeyne.datascrambler.shell.ShellHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Service
public class ConfigService {

    @Lazy
    @Autowired
    ShellHelper shellHelper;

    @Lazy
    @Autowired
    InputReader inputReader;

    @Autowired
    private EncryptService encryptService;

    public static final String CONFIG_JSON = "config.json";


    public boolean doesFileExist() {
        File f = new File(CONFIG_JSON);
        if (f.exists() && !f.isDirectory()) {
            shellHelper.printSuccess("Config file found.");
            return true;
        } else {
            shellHelper.printWarning("No config file found. Please provide us with some information on your source database.");
            return false;
        }
    }

    public Config loadConfig() {
        JsonObject configObj = new JsonObject();
        if (doesFileExist()) {
            configObj = readConfigObj();
        } else {
            //setup
            configObj.put("host", promptUser("Host"));
            configObj.put("port", Integer.parseInt(promptUser("Port")));
            configObj.put("username", promptUser("Username"));
            configObj.put("password", encryptService.encrypt(promptUser("Password")));
            configObj.put("dbName", promptUser("Database name"));
            configObj.put("dbType", promptUser("DB type"));

            persistConfig(configObj);
        }

        return new Config(configObj, encryptService);
    }

    private void persistConfig(JsonObject configObj) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(CONFIG_JSON, "UTF-8");
            writer.println(JsonWriter.string(configObj));
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Impossible to create a config file, check if directory is not read-only.");
        } catch (Exception e) {
            throw new RuntimeException("Could not read file: " + e.getMessage());
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private JsonObject readConfigObj() {
        StringBuilder sb = new StringBuilder();
        try (Stream<String> stream = Files.lines(Paths.get(CONFIG_JSON))) {
            stream.forEach(s -> sb.append(s));
        } catch (IOException e) {
            throw new RuntimeException("Could not read config file: " + e.getMessage());
        }

        try {
            return JsonParser.object().from(sb.toString());
        } catch (JsonParserException e) {
            throw new RuntimeException("Could not read config file: " + e.getMessage());
        }
    }

    private String promptUser(String prompt) {
        String resultString = null;
        do {
            String result = inputReader.prompt(prompt);
            if (StringUtils.hasText(result)) {
                resultString = result;
            } else {
                shellHelper.printWarning("Can not be empty. Please enter valid value.");
            }
        } while (resultString == null);
        return resultString;
    }
}
