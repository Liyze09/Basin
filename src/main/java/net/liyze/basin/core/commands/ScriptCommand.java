package net.liyze.basin.core.commands;

import net.liyze.basin.core.Command;
import net.liyze.basin.core.Conversation;
import net.liyze.basin.core.Main;
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
public class ScriptCommand implements Command {
    @Override
    public void run(@NotNull List<String> args) {
        try (
                BufferedReader script = new BufferedReader(new FileReader(Main.userHome + args.get(0), StandardCharsets.UTF_8))
        ) {
            Conversation conversation = new Conversation().sync();
            Stream<String> lines = script.lines();
            lines.forEach(i -> {
                if (!(i).isEmpty()) conversation.parse(i);
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
