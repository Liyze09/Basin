package net.liyze.basin.core;


public abstract class Util {

    public static void register(Command cmd) {
        Main.commands.put(cmd.Name(), cmd);
    }
}
