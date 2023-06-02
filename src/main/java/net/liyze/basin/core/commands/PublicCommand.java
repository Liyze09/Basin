package net.liyze.basin.core.commands;

import net.liyze.basin.interfaces.Command;

import java.util.List;

import static net.liyze.basin.core.Main.publicRunCommand;

public class PublicCommand implements Command {
    @Override
    public void run(List<String> args) {
        publicRunCommand(String.join(" ", args));
    }

    @Override
    public String Name() {
        return null;
    }
}
