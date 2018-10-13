package com.raida.tech.cloudcoin.utilities;

/*
  Copyright (c) 2018 Ben Ward, 07/06/18
  This work is licensed under the terms of the MIT license.
  For a copy, see <https://opensource.org/licenses/MIT>.
 */

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Logger {

    public static void logFile(String folder, String filename, byte[] contents) {
        Path path = Paths.get("C:/CloudCoins-Java-Server/Logs/" + folder + "/" + filename);

        try {
            Files.deleteIfExists(path);
            Files.createFile(path);

            Files.write(path, contents);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (!Files.exists(path)) {
                System.out.println("Error: file could not be created: " + folder + "/" + filename);
            }
        }
    }

    public static void emptyFolder(String folder) {
        File directory = new File("C:/CloudCoins-Java-Server/Logs/" + folder + "/");
        try {
            if (!directory.exists())
                if (!directory.createNewFile())
                    return;

            for (File file : directory.listFiles())
                if (!file.isDirectory())
                    file.delete();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}