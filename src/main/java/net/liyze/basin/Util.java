package net.liyze.basin;


import static net.liyze.basin.Main.commands;

public abstract class Util {

    public static void register(Command cmd) {
        commands.put(cmd.Name(), cmd);
    }
}
