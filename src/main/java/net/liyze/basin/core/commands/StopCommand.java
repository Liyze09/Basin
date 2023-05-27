package net.liyze.basin.core.commands;

import net.liyze.basin.api.Command;
import net.liyze.basin.core.Basin;

import java.util.List;

/**
 * Stop basin after all task finished.
 *
 * @author Liyze09
 */
public class StopCommand implements Command {
    @Override
    public void run(List<String> args) {
        Basin.shutdown();
    }

    @Override
    public String Name() {
        return "stop";
    }
}