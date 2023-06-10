package net.liyze.basin.core;

import net.liyze.basin.context.annotation.ComponentScan;
import net.liyze.basin.http.HttpServer;
import net.liyze.basin.remote.RemoteServer;
import net.liyze.basin.script.Parser;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;

import static net.liyze.basin.core.Main.*;
import static net.liyze.basin.http.HttpServer.runningServer;
import static net.liyze.basin.remote.RemoteServer.servers;
import static net.liyze.basin.script.Parser.cs;

/**
 * Basin's data class.
 */
@ComponentScan({"net.liyze.basin.script.commands", "net.liyze.basin.script.preparser"})
@SuppressWarnings({"SameReturnValue"})
public final class Basin {
    /**
     * The singleton.
     */
    private static final Basin b = new Basin();
    /**
     * Basin's ASCII banner
     */
    @SuppressWarnings("SpellCheckingInspection")
    public String banner = String.format(
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

    private Basin() {
    }

    /**
     * Get the singleton
     */
    public static Basin getBasin() {
        return b;
    }

    /**
     * Get version version's String.
     */
    @Contract(pure = true)
    public @NotNull String getVersion() {
        return "0.1";
    }

    /**
     * Get version version's int.
     */
    public int getVersionNum() {
        return 1;
    }

    /**
     * Stop basin after all task finished.
     */
    public void shutdown() {
        Main.LOGGER.info("Stopping\n");
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

    /**
     * Restart basin.
     */
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
        servers.forEach(Server::stop);
        servers.clear();
        runningServer.values().forEach(HttpServer::stop);
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
        if (!cfg.startCommand.isBlank()) CONSOLE_PARSER.parse(cfg.startCommand);
        if (cfg.enableRemote && !cfg.accessToken.isBlank()) {
            try {
                new RemoteServer(cfg.accessToken, cfg.remotePort, new Parser()).start();
            } catch (Exception e) {
                LOGGER.error(e.toString());
            }
        }
        LOGGER.info("Restarted!");
    }
}
