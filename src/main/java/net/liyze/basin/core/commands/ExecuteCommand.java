package net.liyze.basin.core.commands;

import net.liyze.basin.core.Main;
import net.liyze.basin.interfaces.Command;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.liyze.basin.core.Main.publicRunCommand;

/**
 * Put command into a CachedThreadPool
 * /execute [command] [args..]
 *
 * @author Liyze09
 */
public class ExecuteCommand implements Command {


    @Override
    public void run(@NotNull List<String> args) {
        Main.servicePool.submit(new Thread(() -> publicRunCommand(args)));
    }

    @Override
    public @NotNull String Name() {
        return "execute";
    }
}


