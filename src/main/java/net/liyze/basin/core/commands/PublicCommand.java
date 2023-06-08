package net.liyze.basin.core.commands;

import net.liyze.basin.core.Conversation;
import net.liyze.basin.interfaces.Command;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.liyze.basin.core.Main.publicVars;

public class PublicCommand implements Command {
    @Override
    public void run(@NotNull List<String> args) {
        Conversation conversation = new Conversation();
        conversation.sync().parse(args);
        publicVars.putAll(conversation.vars);
    }

    @Override
    public @NotNull String Name() {
        return "public";
    }
}

