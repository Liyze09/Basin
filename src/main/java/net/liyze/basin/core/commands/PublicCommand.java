package net.liyze.basin.core.commands;

import net.liyze.basin.interfaces.Command;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.liyze.basin.core.Main.publicRunCommand;

public class PublicCommand implements Command {
    @Override
    public void run(@NotNull List<String> args) {
        publicRunCommand(args);
    }

    @Override
    public @NotNull String Name() {
        return "public";
    }
}

