package net.liyze.basin.script.commands;

import net.liyze.basin.context.annotation.Component;
import net.liyze.basin.script.Command;
import net.liyze.basin.script.exp.ExpParser;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

@Component
public class RunScript implements Command {
    @Override
    public void run(@NotNull List<String> args) {
        try {
            new ExpParser().parse(new StringReader(args.get(0)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull String Name() {
        return "exp";
    }
}
