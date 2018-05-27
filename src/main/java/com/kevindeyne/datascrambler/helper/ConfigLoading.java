package com.kevindeyne.datascrambler.helper;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import com.grack.nanojson.JsonWriter;
import com.kevindeyne.datascrambler.domain.Config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class ConfigLoading {

    public static final String NO_CONFIG_FILE = "No config file was found. Please adjust the new file created in root. ";
    public static final String CONFIG_JSON = "config.json";

    private static void buildSampleConfigFile() {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(CONFIG_JSON, "UTF-8");
            String json = JsonWriter.string()
                    .object()
                    .value("type", "mysql")
                    .value("host", "localhost")
                    .value("port", 3306)
                    .value("username", "datareader")
                    .value("password", "d@tar3ader")
                    .end()
                    .done();
            writer.println(json);

            throw new RuntimeException(NO_CONFIG_FILE);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Impossible to create a config file, check if directory is not read-only.");
        } catch (Exception e) {
            throw new RuntimeException("Could not read file: " + e.getMessage());
        } finally {
            if(writer != null){
                writer.close();
            }
        }
    }

    public static void doesFileExist() {
        System.out.println("Checking if config file exists");
        File f = new File(CONFIG_JSON);
        if(f.exists() && !f.isDirectory()) {
            System.out.println("Config file found");
        } else {
            buildSampleConfigFile();
        }
    }

    private static String loadConfig(){
        StringBuilder sb = new StringBuilder();
        try (Stream<String> stream = Files.lines(Paths.get(CONFIG_JSON))) {
            stream.forEach(s -> sb.append(s));
        } catch (IOException e) {
            return "Could not read config file: " + e.getMessage();
        }
        return sb.toString();
    }

    private static JsonObject parseConfig(String jsonString) {
        try {
            return JsonParser.object().from(jsonString);
        } catch (JsonParserException e) {
            throw new RuntimeException("Could not read config file: " + e.getMessage());
        }
    }

    /**
     * Retrieves config file or creates a dummy one if required
     * @return parsed JSONObject or RuntimeException with relevant message
     */
    public static Config getConfigFile() {
        String config = loadConfig();
        JsonObject obj = parseConfig(config);
        return new Config(obj);
    }

}
