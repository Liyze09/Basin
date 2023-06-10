package net.liyze.basin.script.preparser;

import net.liyze.basin.script.Parser;
import net.liyze.basin.script.PreParser;

public class VariablePointer extends PreParser {
    public VariablePointer(Parser parser) {
        super(parser);
    }

    @Override
    public String getRegex() {
        return "$\\w+";
    }

    @Override
    public String apply(String s) {
        return super.parser.vars.get(s);
    }
}
