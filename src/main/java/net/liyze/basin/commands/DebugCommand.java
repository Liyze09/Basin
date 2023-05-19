package net.liyze.basin.commands;

import net.liyze.basin.Command;

import java.util.ArrayList;

import static net.liyze.basin.Main.*;
import static net.liyze.basin.util.Out.info;

public class DebugCommand implements Command {
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void run(ArrayList<String> args) {
        if (args.contains("true")) {
            debug = true;
            info("debug_mode: true");
            LOGGER.atTrace();
        } else if (args.contains("false")) {
            debug = false;
            info("debug_mode: false");
            LOGGER.atTrace();
        } else {
            info("debug_mode: " + debug);
        }
    }

    @Override
    public String Name() {
        return "debug";
    }
}
