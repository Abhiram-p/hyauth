package dev.hyauth.logger;

import org.apache.logging.log4j.LogManager;

public class AlternativeAuthLoggerManager {
    private static final AlternativeAuthLogger logger = new AlternativeAuthLogger(LogManager.getLogger("alternative-auth"));

    public static void configureLogger(boolean debugMode) {
        logger.setDebugMode(debugMode);
    }

    public static AlternativeAuthLogger getLogger() {
        return logger;
    }
}

