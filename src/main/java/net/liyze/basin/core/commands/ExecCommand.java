package net.liyze.basin.core.commands;

import net.liyze.basin.interfaces.Command;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

import static net.liyze.basin.core.Main.LOGGER;

public class ExecCommand implements Command {
    @Override
    public void run(@NotNull List<String> args) {
        try {
            System.out.println(Runtime.getRuntime().exec(args.toArray(new String[0])).toString());
        } catch (IOException e) {
            LOGGER.error(e.toString());
        }
    }

    @Override
    public @NotNull String Name() {
        return "exec";
    }
}