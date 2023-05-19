package net.liyze.basin.util;


import static net.liyze.basin.Basin.shutdown;
import static net.liyze.basin.Main.*;

@SuppressWarnings("unused")
public abstract class Out {
    public static void fatal(String msg) {
        LOGGER.error(msg);
        shutdown();
    }

    public static void error(String msg) {
        LOGGER.error(msg);
    }

    public static void warn(String msg) {
        LOGGER.warn(msg);
    }

    public static void info(String msg) {
        LOGGER.info(msg);
    }

    public static void debug(String msg) {
        if (debug) LOGGER.info(msg);
    }
}
