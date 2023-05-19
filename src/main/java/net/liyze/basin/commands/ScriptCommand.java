package net.liyze.basin.commands;

import net.liyze.basin.Command;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.stream.Stream;

import static net.liyze.basin.Main.userHome;
import static net.liyze.basin.RunCommands.runCommand;

/**
 * Load a script like
 * <p>
 * {@code
 * execute bench
 * equation 2x+1=3
 * stop
 * }
 * </p>
 *
 * @author Liyze09
 */
public class ScriptCommand implements Command {
    @Override
    public void run(ArrayList<String> args) {
        try (
                BufferedReader script = new BufferedReader(new FileReader(userHome + args.get(0), StandardCharsets.UTF_8))
        ) {
            Stream<String> lines = script.lines();
            lines.forEach(i -> {
                if (!(i).isEmpty()) runCommand(i);
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String Name() {
        return "script";
    }
}
