package com.obfuscator.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LoggerUtil {

    private static final String LOG_DIR = "logs";
    private static final String LOG_FILE = "obfuscator.log";
    private static boolean initialized = false;

    private LoggerUtil() {
    }

    public static synchronized void initialize() {
        if (initialized) {
            return;
        }

        try {
            Path logDirPath = Paths.get(LOG_DIR);
            if (!Files.exists(logDirPath)) {
                Files.createDirectories(logDirPath);
            }

            System.setProperty("log.dir", LOG_DIR);
            System.setProperty("log.file", LOG_FILE);

            LoggerContext context = (LoggerContext) LogManager.getContext(false);
            context.reconfigure();

            initialized = true;
            getLogger(LoggerUtil.class).info("Logging system initialized");

        } catch (IOException e) {
            System.err.println("Failed to initialize logging system: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Logger getLogger(Class<?> clazz) {
        initialize();
        return LogManager.getLogger(clazz);
    }

    public static Logger getLogger(String name) {
        initialize();
        return LogManager.getLogger(name);
    }

    public static void setLogLevel(String loggerName, org.apache.logging.log4j.Level level) {
        Configurator.setLevel(loggerName, level);
    }

    public static Path getLogFilePath() {
        return Paths.get(LOG_DIR, LOG_FILE);
    }

    public static void clearLogs() {
        try {
            Path logFile = getLogFilePath();
            if (Files.exists(logFile)) {
                Files.write(logFile, new byte[0]);
                getLogger(LoggerUtil.class).info("Logs cleared");
            }
        } catch (IOException e) {
            getLogger(LoggerUtil.class).error("Failed to clear logs: {}", e.getMessage());
        }
    }
}
