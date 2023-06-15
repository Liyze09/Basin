package net.liyze.basin.script.commands;

import net.liyze.basin.context.annotation.Component;
import net.liyze.basin.script.Command;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Component
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
