package net.liyze.basin.core;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.liyze.basin.core.Basin.LOGGER;

/**
 * Basin can use as a Basin Plugin
 */
@SuppressWarnings("unused")
public final class InternalPluginDemo implements Command, BasinBoot {
    /**
     * Command
     */
    @Override
    public void run(@NotNull List<String> args) {
        LOGGER.info("Testing");
    }

    /**
     * Command
     */
    @Override
    public @NotNull String Name() {
        return "__test__";
    }

    /**
     * Basin Boot
     */
    @Override
    public void onStart() {
        LOGGER.info("Test: onStart");
    }

    /**
     * Basin Boot
     */
    @Override
    public void afterStart() {
        LOGGER.info("Test: afterStart");
    }

    /**
     * Basin Boot
     */
    @Override
    public void beforeStop() {
        LOGGER.info("Test: beforeStop");
    }
}
