package net.liyze.basin.script;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Basin Command's SPI
 */
public interface Command {
    /**
     * What to do when start the command
     *
     * @author Liyze09
     */
    void run(@NotNull List<String> args);

    /**
     * The Name of the command
     */
    @NotNull String Name();
}
