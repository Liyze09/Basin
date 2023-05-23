package net.liyze.basin.commands;

import net.liyze.basin.Command;
import net.liyze.basin.web.Server;

import java.util.ArrayList;

import static net.liyze.basin.web.Server.runningServer;

public class ServerCommand implements Command {
    @Override
    public void run(ArrayList<String> args) {
        String name = args.get(1);
        Server server;
        try {
            if (args.get(0).equals("stop")) {
                server = runningServer.get(name);
                if (server != null) {
                    server.stop();
                    runningServer.remove(name);
                }
            } else throw new IndexOutOfBoundsException();
        } catch (IndexOutOfBoundsException ignored) {
            int port = Integer.parseInt(args.get(0));
            try {
                server = new Server(name, port);
                runningServer.put(args.get(1), server.run());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public String Name() {
        return "server";
    }

}
