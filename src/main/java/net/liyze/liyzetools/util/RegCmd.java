package net.liyze.liyzetools.util;

import net.liyze.liyzetools.Command;

import static net.liyze.liyzetools.Main.commands;

public abstract class RegCmd {
    public static void register(Command cmd) {
        commands.put(cmd.Name(), cmd);
    }
}
