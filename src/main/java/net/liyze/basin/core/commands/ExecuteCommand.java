package net.liyze.basin.core.commands;

import com.itranswarp.summer.context.annotation.Component;
import net.liyze.basin.core.Command;
import net.liyze.basin.core.CommandParser;
import net.liyze.basin.core.Main;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Put command into a CachedThreadPool
 * /execute [command] [args..]
 *
 * @author Liyze09
 */
@Component
public class ExecuteCommand implements Command {


    @Override
    public void run(@NotNull List<String> args) {
        Main.servicePool.submit(new Thread(() -> new CommandParser().sync().parse(args)));
    }

    @Override
    public @NotNull String Name() {
        return "execute";
    }
}


