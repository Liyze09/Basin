package net.liyze.basin.script;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public abstract class AbstractPreParser implements Function<String, String> {
    protected final Parser parser;

    /**
     *Input the parser start this pre-parser.
     */
    @ApiStatus.Internal
    public AbstractPreParser(Parser parser) {
        this.parser = parser;
    }

    /**
     *Pre-parse regex.
     */
    public abstract @NotNull String getRegex();
}
