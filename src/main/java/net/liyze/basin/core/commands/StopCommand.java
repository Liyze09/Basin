package net.liyze.basin.core.commands;

import net.liyze.basin.core.Basin;
import net.liyze.basin.core.Command;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Stop basin after all task finished.
 *
 * @author Liyze09
 */
public class StopCommand implements Command {
    @Override
    public void run(@NotNull List<String> args) {
        Basin.getBasin().shutdown();
    }

    @Override
    public @NotNull String Name() {
        return "stop";
    }
}
