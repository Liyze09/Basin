package net.liyze.basin.script;

import java.util.function.Function;

public abstract class PreParser implements Function<String, String> {
    protected final Parser parser;

    public PreParser(Parser parser) {
        this.parser = parser;
    }

    public abstract String getRegex();
}
