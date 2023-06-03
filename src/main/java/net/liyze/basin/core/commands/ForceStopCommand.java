package net.liyze.basin.core.commands;

import net.liyze.basin.interfaces.Command;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ForceStopCommand implements Command {
    @Override
    public void run(@NotNull List<String> args) {
        System.exit(0);
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Override
    public @NotNull String Name() {
        return "forcestop";
    }
}
