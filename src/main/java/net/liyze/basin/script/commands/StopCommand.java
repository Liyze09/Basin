package net.liyze.basin.script.commands;

import net.liyze.basin.context.annotation.Component;
import net.liyze.basin.core.Basin;
import net.liyze.basin.script.Command;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Stop basin after all task finished.
 *
 * @author Liyze09
 */
@Component
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
