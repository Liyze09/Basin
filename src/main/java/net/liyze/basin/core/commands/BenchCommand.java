package net.liyze.basin.core.commands;

import net.liyze.basin.core.Command;
import net.liyze.basin.core.Main;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.List;

/**
 * Test your JVM speed
 *
 * @author Liyze09
 */
public class BenchCommand implements Command {
    @SuppressWarnings("unused")
    @Override
    public void run(@NotNull List<String> args) {
        double rpi = 1;
        long t = 3;
        long i = 0;
        long start;
        start = Instant.now().getEpochSecond();
        do {
            if (i / 2 == 0) {
                rpi -= ((double) 1 / t);
            } else {
                rpi += (double) 1 / t;
            }
            ++i;
            t += 2;
        } while (Instant.now().getEpochSecond() != start + 10);
        Main.LOGGER.info(String.valueOf(i));
    }

    @Override
    public @NotNull String Name() {
        return "bench";
    }
}
