package net.liyze.basin.commands;

import net.liyze.basin.Command;

import java.time.Instant;
import java.util.ArrayList;

import static net.liyze.basin.Main.LOGGER;

/**
 * Test your JVM speed
 *
 * @author Liyze09
 */
public class BenchCommand implements Command {
    @SuppressWarnings("unused")
    @Override
    public void run(ArrayList<String> args) {
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
        LOGGER.info(String.valueOf(i));
    }

    @Override
    public String Name() {
        return "bench";
    }
}
