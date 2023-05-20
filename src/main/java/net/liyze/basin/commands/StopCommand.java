package net.liyze.basin.commands;

import net.liyze.basin.Command;

import java.util.ArrayList;

import static net.liyze.basin.Basin.shutdown;

/**
 * Stop basin after all task finished.
 *
 * @author Liyze09
 */
public class StopCommand implements Command {
    @Override
    public void run(ArrayList<String> args) {
        shutdown();
    }

    @Override
    public String Name() {
        return "stop";
    }
}
