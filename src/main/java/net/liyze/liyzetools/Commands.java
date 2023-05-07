package net.liyze.liyzetools;

import net.liyze.liyzetools.commands.*;

import static net.liyze.liyzetools.util.RegCmd.register;

public abstract class Commands {
    public static void regCommands() {
        register(new StopCommand());
        register(new EquationCommand());
        register(new DebugCommand());
        register(new ListCommand());
        register(new BenchCommand());
    }
}
