package net.liyze.basin.core.commands;

import net.liyze.basin.api.Command;

import java.util.ArrayList;

public class ForceStopCommand implements Command {
    @Override
    public void run(ArrayList<String> args) {
        System.exit(0);
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Override
    public String Name() {
        return "forcestop";
    }
}
