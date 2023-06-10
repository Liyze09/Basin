package net.liyze.basin.script.preparser;

import net.liyze.basin.context.annotation.Component;
import net.liyze.basin.context.annotation.WithoutInstance;
import net.liyze.basin.script.Parser;
import net.liyze.basin.script.PreParser;
import org.jetbrains.annotations.NotNull;

import static java.lang.System.out;
@Component
@WithoutInstance
public class Var extends PreParser {
    public Var(Parser parser) {
        super(parser);
    }

    @Override
    public @NotNull String getRegex() {
        return "\\$\\w+";
    }

    @Override
    public String apply(String s) {
        out.println(parser.vars.get(s.substring(1)));
        return parser.vars.get(s.substring(1));
    }
}
