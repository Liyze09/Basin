package net.liyze.basin.core.commands;

import net.liyze.basin.context.annotation.Component;
import net.liyze.basin.core.Command;
import net.liyze.basin.core.CommandParser;
import net.liyze.basin.core.Main;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static net.liyze.basin.core.Main.LOGGER;

/**
 * Load a script like
 * <p>
 * execute bench
 * </p><p>
 * equation 2x+1=3
 * </p><p>
 * stop
 * </p>
 *
 * @author Liyze09
 */
@Component
public class ScriptCommand implements Command {
    @Override
    public void run(@NotNull List<String> args) {
        try {
            BufferedReader script = new BufferedReader(new FileReader(Main.userHome + args.get(0), StandardCharsets.UTF_8));
            CommandParser parser = new CommandParser();
            parser.sync().parseScript(script);
        } catch (IOException e) {
            LOGGER.info(e.toString());
        }
    }

    @Override
    public @NotNull String Name() {
        return "bscript";
    }
}
