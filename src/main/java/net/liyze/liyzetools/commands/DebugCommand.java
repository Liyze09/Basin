package net.liyze.liyzetools.commands;

import net.liyze.liyzetools.Command;

import java.util.ArrayList;

import static net.liyze.liyzetools.Main.debug;
import static net.liyze.liyzetools.util.Out.info;

public class DebugCommand implements Command {
    @Override
    public void run(ArrayList<String> args) {
        if (args.contains("true")) {
            debug = true;
            info("debug_mode: true");
        } else if (args.contains("false")) {
            debug = false;
            info("debug_mode: false");
        } else {
            info("debug_mode: "+debug);
        }
    }

    @Override
    public String Name() {
        return "debug";
    }
}
