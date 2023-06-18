package net.liyze.basin.core.commands;

import net.liyze.basin.context.annotation.Component;
import net.liyze.basin.remote.Client;
import net.liyze.basin.core.Command;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.liyze.basin.core.Main.LOGGER;
import static net.liyze.basin.core.Main.envMap;

@Component
public class RemoteCommand implements Command {
    @Override
    public void run(@NotNull List<String> args) {
        String host = args.remove(0);
        try {
            Client.send(String.join(" ", args), host, envMap.get("\"" + host + "_token\"").toString());
        } catch (Exception e) {
            LOGGER.error(e.toString());
        }
    }

    @Override
    public @NotNull String Name() {
        return "remote";
    }
}
