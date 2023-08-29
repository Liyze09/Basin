package net.liyze.basin.util;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.io.PrintWriter;
import java.io.StringWriter;

public class LoggingUtils {
    private static final Logger DEFAULT = LoggerFactory.getLogger("EXCEPTION");

    private LoggingUtils() {
        throw new UnsupportedOperationException();
    }

    public static void printException(@NotNull Logger LOGGER, @NotNull Throwable throwable, Level level) {
        var out = new StringWriter();
        var buffer = new PrintWriter(out);
        throwable.printStackTrace(buffer);
        LOGGER.atLevel(level).log(out.toString());
    }

    public static void printException(@NotNull Logger LOGGER, @NotNull Throwable throwable) {
        printException(LOGGER, throwable, Level.ERROR);
    }

    public static void printException(@NotNull Throwable throwable) {
        printException(DEFAULT, throwable, Level.ERROR);
    }
}
