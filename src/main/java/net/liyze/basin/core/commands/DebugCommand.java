package net.liyze.basin.core.commands;

import net.liyze.basin.core.Command;
import net.liyze.basin.core.Main;

import java.util.ArrayList;

/**
 * Is enabled ,print debug
 */
public class DebugCommand implements Command {
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void run(ArrayList<String> args) {
        if (args.contains("true")) {
            Main.debug = true;
            Main.LOGGER.info("debug_mode: true");
            Main.LOGGER.atTrace();
        } else if (args.contains("false")) {
            Main.debug = false;
            Main.LOGGER.info("debug_mode: false");
            Main.LOGGER.atInfo();
        } else {
            Main.LOGGER.info("debug_mode: {}", Main.debug);
        }
    }

    @Override
    public String Name() {
        return "debug";
    }
}
