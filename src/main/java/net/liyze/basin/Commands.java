package net.liyze.basin;

import net.liyze.basin.commands.*;

import static net.liyze.basin.Util.register;

public abstract class Commands {
    public static void regCommands() {
        register(new StopCommand());
        register(new EquationCommand());
        register(new DebugCommand());
        register(new ListCommand());
        register(new BenchCommand());
        register(new ExecuteCommand());
        register(new ScriptCommand());
        register(new ServerCommand());
    }
}
