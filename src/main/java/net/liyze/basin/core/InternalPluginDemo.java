package net.liyze.basin.core;

import net.liyze.basin.interfaces.BasinBoot;
import net.liyze.basin.interfaces.Command;

import java.util.List;

import static net.liyze.basin.core.Main.LOGGER;

@SuppressWarnings("unused")
public final class InternalPluginDemo implements Command, BasinBoot {
    @Override
    public void run(List<String> args) {
        LOGGER.info("Testing");
    }

    @Override
    public String Name() {
        return "__test__";
    }

    @Override
    public void onStart() {
        LOGGER.info("Test: onStart");
    }

    @Override
    public void afterStart() {
        LOGGER.info("Test: afterStart");
    }

    @Override
    public void beforeStop() {
        LOGGER.info("Test: beforeStop");
    }
}
