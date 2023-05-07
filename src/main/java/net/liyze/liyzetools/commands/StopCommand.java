package net.liyze.liyzetools.commands;

import net.liyze.liyzetools.Command;

import java.util.ArrayList;

import static net.liyze.liyzetools.Main.stopAll;

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
