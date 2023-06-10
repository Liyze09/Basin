package net.liyze.basin.script.commands;

import net.liyze.basin.context.annotation.Component;
import net.liyze.basin.core.Basin;
import net.liyze.basin.core.Command;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Component
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
