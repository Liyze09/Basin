package net.liyze.basin.core.commands;

import net.liyze.basin.api.Command;

import java.util.ArrayList;

import static net.liyze.basin.core.Commands.regCommands;
import static net.liyze.basin.core.Loader.*;
import static net.liyze.basin.core.Main.*;

public class RestartCommand implements Command {
    @Override
    public void run(ArrayList<String> args) {
        taskPool.shutdownNow();
        servicePool.shutdownNow();
        commands.clear();
        BootClasses.clear();
        init();
        try {
            loadJars();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        regCommands();
        LOGGER.info("Restarted!");
    }

    @Override
    public String Name() {
        return "restart";
    }
}
