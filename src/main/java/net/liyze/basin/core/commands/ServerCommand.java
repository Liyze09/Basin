package net.liyze.basin.core.commands;

import com.itranswarp.summer.context.annotation.Component;
import net.liyze.basin.core.Basin;
import net.liyze.basin.core.Command;
import net.liyze.basin.http.HttpServer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Component
public class ServerCommand implements Command {
    @Override
    public void run(@NotNull List<String> args) {
        String name = args.get(1);
        HttpServer server;
        try {
            if (args.get(0).equals("stop")) {
                server = HttpServer.runningServer.get(name);
                if (server != null) {
                    server.stop();
                    HttpServer.runningServer.remove(name);
                } else {
                    Basin.LOGGER.error("{} is not exist.", name);
                }
            } else throw new IndexOutOfBoundsException();
        } catch (IndexOutOfBoundsException ignored) {
            int port = Integer.parseInt(args.get(0));
            try {
                server = new HttpServer(name, port);
                HttpServer.runningServer.put(name, server.start());
            } catch (Exception e) {
                Basin.LOGGER.error(e.toString());
            }
        }
    }

    @Override
    public @NotNull String Name() {
        return "server";
    }

}
