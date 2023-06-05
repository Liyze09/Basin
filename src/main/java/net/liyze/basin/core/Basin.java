package net.liyze.basin.core;

import net.liyze.basin.interfaces.BasinBoot;
import net.liyze.basin.web.Server;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;

import static net.liyze.basin.core.Conversation.cs;
import static net.liyze.basin.core.Main.*;
import static net.liyze.basin.remote.Server.servers;
import static net.liyze.basin.web.Server.runningServer;

@SuppressWarnings({"SameReturnValue"})
public final class Basin {
    private Basin() {
    }

    private static final Basin b = new Basin();

    public static Basin getBasin() {
        return b;
    }

    @SuppressWarnings("SpellCheckingInspection")
    public String basin = String.format(
            """
                    \r
                    BBBBBBBBBBBBBBBBB                                         iiii
                    B::::::::::::::::B                                       i::::i
                    B::::::BBBBBB:::::B                                       iiii
                    BB:::B      B:::::B
                    B::::B       B:::::B   aaaaaaaaaaaaa        ssssssssss   iiiiiii   nnnn  nnnnnnnn
                    B::::B      B:::::B   a::::::::::::a     ss::::::::::s   i:::::i  n:::nn::::::::nn
                    B::::BBBBBB:::::B     aaaaaaaaa:::::a  ss:::::::::::::s   i::::i  n::::::::::::::nn
                    B::::::::::::BB                a::::a  s::::::ssss:::::s  i::::i  nn::::::::::::::n
                    B::::BBBBBB:::::B       aaaaaaa:::::a   s:::::s   ssssss  i::::i   n:::::nnnn:::::n
                    B::::B      B:::::B    aa::::::::::::a     s::::::s       i::::i   n::::n    n::::n
                    B::::B       B:::::B  a::::aaaa::::::a        s::::::s    i::::i   n::::n    n::::n
                    B::::B      B:::::B  a::::a    a:::::a  ssssss   s:::::s  i::::i   n::::n    n::::n
                    BB:::::BBBBBB::::::B a::::a    a:::::a s:::::ssss::::::s i::::::i  n::::n    n::::n
                    B:::::::::::::::::B a:::::aaaa::::::a  s::::::::::::::s  i::::::i  n::::n    n::::n
                    B::::::::::::::::B   a::::::::::aa:::a  s:::::::::::ss   i::::::i  n::::n    n::::n
                    BBBBBBBBBBBBBBBBB     aaaaaaaaaa  aaaa   sssssssssss     iiiiiiii  nnnnnn    nnnnnn
                    :: Basin :: (%s)
                    """, getVersion());

    @Contract(pure = true)
    public @NotNull String getVersion() {
        return "0.1";
    }

    public int getVersionNum() {
        return 1;
    }

    /**
     * Stop basin after all task finished.
     */
    public void shutdown() {
        Main.LOGGER.info("Stopping\n\n");
        BootClasses.forEach((i) -> {
            try {
                ((BasinBoot) i.getDeclaredConstructor().newInstance()).beforeStop();
            } catch (Exception ignored) {
            }
        });
        Main.taskPool.shutdown();
        Main.servicePool.shutdownNow();
        System.exit(0);
    }

    public void restart() {
        BootClasses.forEach((i) -> {
            try {
                BasinBoot in = (BasinBoot) i.getDeclaredConstructor().newInstance();
                in.beforeStop();
            } catch (Exception e) {
                LOGGER.error(e.toString());
            }
        });
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
            BootClasses.forEach((i) -> {
                try {
                    BasinBoot in = (BasinBoot) i.getDeclaredConstructor().newInstance();
                    in.afterStart();
                } catch (Exception e) {
                    LOGGER.error(e.toString());
                }
            });
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
}
