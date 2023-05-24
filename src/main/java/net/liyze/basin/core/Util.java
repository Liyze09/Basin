package net.liyze.basin.core;

import net.liyze.basin.api.Command;

public abstract class Util {

    public static void register(Command cmd) {
        Main.commands.put(cmd.Name(), cmd);
    }
}
