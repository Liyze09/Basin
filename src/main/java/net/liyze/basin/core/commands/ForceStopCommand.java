package net.liyze.basin.core.commands;

import net.liyze.basin.api.Command;

import java.util.List;

public class ForceStopCommand implements Command {
    @Override
    public void run(List<String> args) {
        System.exit(0);
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Override
    public String Name() {
        return "forcestop";
    }
}
