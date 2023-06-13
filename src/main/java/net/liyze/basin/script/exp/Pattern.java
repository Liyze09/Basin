package net.liyze.basin.script.exp;

import static net.liyze.basin.script.exp.ExpParser.patterns;

public class Pattern extends Token {
    Patterns pattern;

    public Pattern(String pattern) {
        this.pattern = patterns.get(pattern);
    }
}
