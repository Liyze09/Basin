package net.liyze.basin.core.commands;

import com.itranswarp.summer.context.annotation.Component;
import net.liyze.basin.core.Command;
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
