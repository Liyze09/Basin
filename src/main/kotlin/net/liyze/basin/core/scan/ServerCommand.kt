package net.liyze.basin.core.scan;

import com.itranswarp.summer.annotation.Component;
import net.liyze.basin.core.Basin;
import net.liyze.basin.core.Command;
import net.liyze.basin.core.Server;
import net.liyze.basin.http.HttpServer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ServerCommand implements Command {
    public static final Map<String, Server> serverMap = new ConcurrentHashMap<>();

    @Override
    public void run(@NotNull List<String> args) {
        String name = args.get(0);
        if (!args.get(1).equals("stop")) {
            serverMap.put(name, new HttpServer(name, Integer.parseInt(args.get(1))).start());
        } else {
            Server server = serverMap.remove(name);
            if (server != null) server.stop();
            else Basin.LOGGER.error("Server {} was not exist.", name);
        }
    }

    @Override
    public @NotNull String Name() {
        return "server";
    }
}
