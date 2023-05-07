package net.liyze.liyzetools.util;

import static net.liyze.liyzetools.Main.debug;
import static net.liyze.liyzetools.Main.stopAll;

public abstract class Out {
    public static void fatal(String msg) {
        System.out.println("FATAL: " + msg);
        stopAll();
    }

    public static void error(String msg) {
        System.out.println("ERROR: " + msg);
    }

    public static void warn(String msg) {
        System.out.println("WARN: " + msg);
    }

    public static void info(String msg) {
        System.out.println("INFO: " + msg);
    }

    public static void debug(String msg) {
        if (debug) System.out.println(msg);
    }
}
