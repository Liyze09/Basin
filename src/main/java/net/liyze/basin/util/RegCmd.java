package net.liyze.basin.util;

import net.liyze.basin.Command;

import static net.liyze.basin.Main.commands;

public abstract class RegCmd {
    public static void register(Command cmd) {
        commands.put(cmd.Name(), cmd);
    }
}
