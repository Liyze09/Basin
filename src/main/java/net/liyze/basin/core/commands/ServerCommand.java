package net.liyze.basin.core.commands;

import net.liyze.basin.core.Main;
import net.liyze.basin.http.Server;
import net.liyze.basin.interfaces.Command;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ServerCommand implements Command {
    @Override
    public void run(@NotNull List<String> args) {
        String name = args.get(1);
        Server server;
        try {
            if (args.get(0).equals("stop")) {
                server = Server.runningServer.get(name);
                if (server != null) {
                    server.stop();
                    Server.runningServer.remove(name);
                } else {
                    Main.LOGGER.error("{} is not exist.", name);
                }
            } else throw new IndexOutOfBoundsException();
        } catch (IndexOutOfBoundsException ignored) {
            int port = Integer.parseInt(args.get(0));
            try {
                server = new Server(name, port);
                Server.runningServer.put(name, server.run());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public @NotNull String Name() {
        return "server";
    }

}
