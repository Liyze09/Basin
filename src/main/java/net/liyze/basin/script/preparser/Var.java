package net.liyze.basin.script.preparser;

import net.liyze.basin.script.Parser;
import net.liyze.basin.script.PreParser;
import org.jetbrains.annotations.NotNull;

public class Var extends PreParser {
    public Var(Parser parser) {
        super(parser);
    }

    @Override
    public @NotNull String getRegex() {
        return "\\$.*";
    }

    @Override
    public String apply(String s) {
        return parser.vars.get(s.replaceFirst("$", ""));
    }
}
