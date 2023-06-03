package net.liyze.basin.core.commands;

import net.liyze.basin.interfaces.Command;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.Executors;

import static net.liyze.basin.core.Main.*;

public class RestartCommand implements Command {
    @Override
    public void run(@NotNull List<String> args) {
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
            e.printStackTrace();
        }
        try {
            loadJars();
        } catch (Exception e) {
            e.printStackTrace();
        }
        regCommands();
        LOGGER.info("Restarted!");
    }

    @Override
    public @NotNull String Name() {
        return "restart";
    }
}
