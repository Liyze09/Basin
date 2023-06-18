package net.liyze.basin.core.commands;

import net.liyze.basin.context.annotation.Component;
import net.liyze.basin.core.Command;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

import static net.liyze.basin.core.Main.*;

@Component
public class ShellCommand implements Command {
    @Override
    public void run(@NotNull List<String> args) {
        if (!cfg.enableShellCommand) return;
        try {
            LOGGER.info(Runtime.getRuntime().exec(args.toArray(new String[0])).toString());
        } catch (IOException e) {
            LOGGER.error(e.toString());
        }
    }

    @Override
    public @NotNull String Name() {
        return "exec";
    }
}
