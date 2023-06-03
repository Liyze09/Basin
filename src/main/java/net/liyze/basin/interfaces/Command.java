package net.liyze.basin.interfaces;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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

    @SuppressWarnings("unused")
    default @Nullable Object returns() {
        return null;
    }
}
