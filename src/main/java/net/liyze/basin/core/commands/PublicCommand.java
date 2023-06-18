package net.liyze.basin.core.commands;

import net.liyze.basin.context.annotation.Component;
import net.liyze.basin.core.Command;
import net.liyze.basin.core.CommandParser;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.liyze.basin.core.Main.publicVars;

@Component
public class PublicCommand implements Command {
    @Override
    public void run(@NotNull List<String> args) {
        CommandParser parser = new CommandParser();
        parser.sync().parse(args);
        publicVars.putAll(parser.vars);
    }

    @Override
    public @NotNull String Name() {
        return "public";
    }
}

