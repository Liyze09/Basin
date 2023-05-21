package net.liyze.basin.commands;

import net.liyze.basin.Command;
import net.liyze.basin.web.Server;

import java.util.ArrayList;

public class ServerCommand implements Command {

    @Override
    public void run(ArrayList<String> args) {
        int port = Integer.parseInt(args.get(0));
        String name = args.get(1);
        try {
            new Server(name, port).run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String Name() {
        return "server";
    }
}
