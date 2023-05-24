package net.liyze.basin.core.commands;

import net.liyze.basin.core.Command;
import net.liyze.basin.core.Basin;

import java.util.ArrayList;

/**
 * Stop basin after all task finished.
 *
 * @author Liyze09
 */
public class StopCommand implements Command {
    @Override
    public void run(ArrayList<String> args) {
        Basin.shutdown();
    }

    @Override
    public String Name() {
        return "stop";
    }
}
