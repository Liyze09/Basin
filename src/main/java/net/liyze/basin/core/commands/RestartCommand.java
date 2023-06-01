package net.liyze.basin.core.commands;

import net.liyze.basin.interfaces.Command;

import java.util.List;
import java.util.concurrent.Executors;

import static net.liyze.basin.core.Main.*;

public class RestartCommand implements Command {
    @Override
    public void run(List<String> args) {
        taskPool.shutdownNow();
        servicePool.shutdownNow();
        commands.clear();
        BootClasses.clear();
        publicVars.clear();
        taskPool = Executors.newFixedThreadPool(cfg.taskPoolSize);
        servicePool = Executors.newCachedThreadPool();
        try {
            init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
