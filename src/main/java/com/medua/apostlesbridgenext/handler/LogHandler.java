package com.medua.apostlesbridgenext.handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LogHandler {
    private final Class<?> logClass;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final boolean debug = false;

    private final Logger logger;

    public LogHandler(Class<?> logClass) {
        this.logClass = logClass;

        this.logger = LogManager.getLogger(logClass);
    }

    private String getTimestamp() {
        return dateFormat.format(new Date());
    }

    public void debug(String message) {
        if (debug) {
            System.out.println("[" + getTimestamp() + "] [DEBUG] [" + logClass.getSimpleName() + "] " + message);
        }
    }

    public void info(String message) {
        logger.info("[" + getTimestamp() + "] [INFO] [" + logClass.getSimpleName() + "] " + message);
    }

    public void warn(String message) {
        logger.warn("[" + getTimestamp() + "] [WARNING] [" + logClass.getSimpleName() + "] " + message);
    }

    public void error(String message) {
        logger.error("[" + getTimestamp() + "] [ERROR] [" + logClass.getSimpleName() + "] " + message);
    }
}
