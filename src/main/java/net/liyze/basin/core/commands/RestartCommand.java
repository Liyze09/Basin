package net.liyze.basin.core.commands;

import net.liyze.basin.core.Basin;
import net.liyze.basin.interfaces.Command;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RestartCommand implements Command {
    @Override
    public void run(@NotNull List<String> args) {
        Basin.getBasin().restart();
    }

    @Override
    public @NotNull String Name() {
        return "restart";
    }
}
