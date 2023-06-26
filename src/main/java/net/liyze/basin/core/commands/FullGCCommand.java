package net.liyze.basin.core.commands;

import com.itranswarp.summer.context.annotation.Component;
import net.liyze.basin.core.Command;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.liyze.basin.core.Main.LOGGER;

@Component
public class FullGCCommand implements Command {
    @Override
    public void run(@NotNull List<String> args) {
        System.gc();
        LOGGER.info("Full GC");
    }

    @Override
    public @NotNull String Name() {
        return "fgc";
    }
}
