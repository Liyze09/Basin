package net.liyze.basin.core;

import net.liyze.basin.commands.*;
import net.liyze.basin.core.commands.*;

import static net.liyze.basin.core.Util.register;

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
