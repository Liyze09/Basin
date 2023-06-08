package net.liyze.basin.core;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    @ApiStatus.Experimental
    @SuppressWarnings("unused")
    default @Nullable Object returns() {
        return null;
    }
}
