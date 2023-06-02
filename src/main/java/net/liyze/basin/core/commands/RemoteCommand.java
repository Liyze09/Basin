package net.liyze.basin.core.commands;

import net.liyze.basin.interfaces.Command;
import net.liyze.basin.remote.Client;

import java.util.List;

import static net.liyze.basin.core.Main.LOGGER;

public class RemoteCommand implements Command {
    @Override
    public void run(List<String> args) {
        String host = args.get(0);
        args.remove(0);
        try {
            Client.send(String.join(" ", args), host);
        } catch (Exception e) {
            LOGGER.error(e.toString());
        }
    }

    @Override
    public String Name() {
        return "remote";
    }
}
