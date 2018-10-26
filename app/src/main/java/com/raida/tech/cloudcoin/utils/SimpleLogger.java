package com.raida.tech.cloudcoin.utils;

import com.raida.tech.cloudcoin.core.FileSystem;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.raida.tech.cloudcoin.utils.SimpleLogger.LogLevel.INFO;

public class SimpleLogger {


    /* Fields */

    private String Filename;

    /// <summary>
    /// Initialize a new instance of SimpleLogger class.
    /// Log file will be created automatically if not yet exists, else it can be either a fresh new file or append to the existing file.
    /// Default is create a fresh new log file.
    /// </summary>
    /// <param name="append">True to append to existing log file, False to overwrite and create new log file</param>


    public SimpleLogger(String FileName, boolean append) {
        initialize(FileName, append);
    }

    private void initialize(String FileName, boolean append) {
        this.Filename = FileName;
        File file = new File(Filename);
        String logHeader = Filename + " is created.";
        if (!file.exists()) {
            WriteFormattedLog(INFO, logHeader);
        } else {
            if (!append)
                WriteFormattedLog(INFO, logHeader);
        }
    }


    /// <summary>
    /// Log an info message
    /// </summary>
    /// <param name="text">Message</param>
    public void Info(String text) {
        WriteFormattedLog(INFO, text);
    }

    /// <summary>
    /// Format a log message based on log level
    /// </summary>
    /// <param name="level">Log level</param>
    /// <param name="text">Log message</param>
    private void WriteFormattedLog(LogLevel level, String text) {
        String pretext;
        switch (level) {
            case TRACE:
                pretext = FileSystem.getFormattedTime() + " [TRACE]   ";
                break;
            case INFO:
                pretext = FileSystem.getFormattedTime() + " [INFO]    ";
                break;
            case DEBUG:
                pretext = FileSystem.getFormattedTime() + " [DEBUG]   ";
                break;
            case WARNING:
                pretext = FileSystem.getFormattedTime() + " [WARNING] ";
                break;
            case ERROR:
                pretext = FileSystem.getFormattedTime() + " [ERROR]   ";
                break;
            case FATAL:
                pretext = FileSystem.getFormattedTime() + " [FATAL]   ";
                break;
            default:
                pretext = "";
                break;
        }

        WriteLine("\n" + pretext + text);
    }

    /// <summary>
    /// Write a line of formatted log message into a log file
    /// </summary>
    /// <param name="text">Formatted log message</param>
    /// <param name="append">True to append, False to overwrite the file</param>
    /// <exception cref="System.IO.IOException"></exception>
  /*  private void WriteLine(String text) {
        try {
            StandardOpenOption option = StandardOpenOption.TRUNCATE_EXISTING;

            Path path = Paths.get(Filename);
            if (!Files.exists(path)) {
                Files.createDirectories(path.getParent());
                Files.createFile(path);
            }
            Files.write(path, text.getBytes(StandardCharsets.UTF_8), option);
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }*/

    private void WriteLine(String text) {

        BufferedWriter bw = null;
        FileWriter fw = null;

        try {

            File file = new File(Filename);
            if (!file.exists()) {
                file.createNewFile();
            }
            fw = new FileWriter(file.getAbsoluteFile(), true);
            bw = new BufferedWriter(fw);

            bw.write(text);

            System.out.println("Done");

        } catch (IOException e) {

            e.printStackTrace();

        } finally {

            try {

                if (bw != null)
                    bw.close();

                if (fw != null)
                    fw.close();

            } catch (IOException ex) {

                ex.printStackTrace();

            }
        }

    }

    /// <summary>
    /// Supported log level
    /// </summary>
    //[Flags]
    enum LogLevel {
        TRACE,
        INFO,
        DEBUG,
        WARNING,
        ERROR,
        FATAL
    }
}
