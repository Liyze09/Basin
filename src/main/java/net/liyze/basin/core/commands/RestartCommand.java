package net.liyze.basin.core.commands;

import net.liyze.basin.core.Conversation;
import net.liyze.basin.interfaces.BasinBoot;
import net.liyze.basin.interfaces.Command;
import net.liyze.basin.web.Server;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.Executors;

import static net.liyze.basin.core.Conversation.cs;
import static net.liyze.basin.core.Main.*;
import static net.liyze.basin.remote.Server.servers;
import static net.liyze.basin.web.Server.runningServer;

public class RestartCommand implements Command {
    @Override
    public void run(@NotNull List<String> args) {
        BootClasses.forEach((i) -> new Thread(() -> {
            try {
                BasinBoot in = (BasinBoot) i.getDeclaredConstructor().newInstance();
                in.beforeStop();
            } catch (Exception e) {
                LOGGER.error(e.toString());
            }
        }).start());
        cs.forEach(c -> c.vars.clear());
        taskPool.shutdownNow();
        servicePool.shutdownNow();
        servers.forEach(net.liyze.basin.remote.Server::shutdown);
        servers.clear();
        runningServer.values().forEach(Server::stop);
        runningServer.clear();
        commands.clear();
        BootClasses.clear();
        publicVars.clear();
        taskPool = Executors.newFixedThreadPool(cfg.taskPoolSize);
        servicePool = Executors.newCachedThreadPool();
        try {
            init();
        } catch (Exception e) {
            LOGGER.error(e.toString());
        }
        try {
            loadJars();
            BootClasses.forEach((i) -> new Thread(() -> {
                try {
                    BasinBoot in = (BasinBoot) i.getDeclaredConstructor().newInstance();
                    in.afterStart();
                } catch (Exception e) {
                    LOGGER.error(e.toString());
                }
            }).start());
        } catch (Exception e) {
            LOGGER.error(e.toString());
        }
        regCommands();
        if (!cfg.startCommand.isBlank()) CONSOLE_CONVERSATION.parse(cfg.startCommand);
        if (cfg.enableRemote && !cfg.accessToken.isBlank()) {
            try {
                new net.liyze.basin.remote.Server(cfg.accessToken, cfg.remotePort, new Conversation()).start();
            } catch (Exception e) {
                LOGGER.error(e.toString());
            }
        }
        LOGGER.info("Restarted!");
    }

    @Override
    public @NotNull String Name() {
        return "restart";
    }
}
