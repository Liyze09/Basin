package net.liyze.liyzetools.commands;

import net.liyze.liyzetools.Command;

import java.time.Instant;
import java.util.ArrayList;

import static net.liyze.liyzetools.util.Out.debug;
import static net.liyze.liyzetools.util.Out.info;

public class BenchCommand implements Command {

    @Override
    public void run(ArrayList<String> args) {
        double rpi = 1,pi;
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
        pi=rpi*4;
        info(String.valueOf(i));
        debug(String.valueOf(pi));
    }

    @Override
    public String Name() {
        return "bench";
    }
}
