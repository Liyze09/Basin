package net.liyze.basin.util;

import static net.liyze.basin.Main.*;
@SuppressWarnings("unused")
public abstract class Out {
    public static void fatal(String msg) {
        logger.error(msg);
        stopAll();
    }

    public static void error(String msg) {
        logger.error(msg);
    }

    public static void warn(String msg) {
        logger.warn(msg);
    }

    public static void info(String msg) {
        logger.info(msg);
    }

    public static void debug(String msg) {
        if (debug) logger.info(msg);
    }
}
