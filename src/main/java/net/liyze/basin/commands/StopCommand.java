package net.liyze.basin.commands;

import net.liyze.basin.Command;

import java.util.ArrayList;

import static net.liyze.basin.Main.stopAll;

public class StopCommand implements Command {
    @Override
    public void run(ArrayList<String> args) {
        stopAll();
    }

    @Override
    public String Name() {
        return "stop";
    }
}
