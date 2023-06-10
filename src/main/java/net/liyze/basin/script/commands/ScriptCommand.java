package net.liyze.basin.script.commands;

import net.liyze.basin.context.annotation.Component;
import net.liyze.basin.core.Command;
import net.liyze.basin.core.Main;
import net.liyze.basin.script.Parser;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

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
        try (
                BufferedReader script = new BufferedReader(new FileReader(Main.userHome + args.get(0), StandardCharsets.UTF_8))
        ) {
            Parser parser = new Parser().sync();
            Stream<String> lines = script.lines();
            lines.forEach(i -> {
                if (!(i).isEmpty()) parser.parse(i);
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull String Name() {
        return "script";
    }
}
