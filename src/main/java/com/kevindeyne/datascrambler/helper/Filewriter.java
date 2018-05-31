package com.kevindeyne.datascrambler.helper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;
import java.nio.file.StandardOpenOption;

public class Filewriter {

    private static final String APPENDER = "_";
    private static final String FILE_FORMAT = ".sql";
    private static final String FOLDER = "downloads";

    public static void writeToFile(String line, String db, int tableNr){
        try{
            createFolderIfNotExists();
            write(line, db, tableNr);
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    private static void write(String line, String db, int tableNr) throws IOException {
        line += "\n";
        Path path = Paths.get(FOLDER + File.separator + tableNr + APPENDER + db + FILE_FORMAT);
        Files.write(path, line.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    private static void createFolderIfNotExists() {
        File dir = new File(FOLDER);
        if (!dir.exists()) dir.mkdirs();
    }


    public static void cleanFolder() {
        File dir = new File(FOLDER);
        deleteFolder(dir);
    }

    private static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if(files!=null) {
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

}
