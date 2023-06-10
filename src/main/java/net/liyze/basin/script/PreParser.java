package net.liyze.basin.script;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public abstract class PreParser implements Function<String, String> {
    protected final Parser parser;

    public PreParser(Parser parser) {
        this.parser = parser;
    }

    public abstract @NotNull String getRegex();
}
