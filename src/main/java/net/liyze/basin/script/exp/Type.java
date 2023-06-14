package net.liyze.basin.script.exp;

import static net.liyze.basin.script.exp.ExpParser.types;

public class Type extends Token {
    public Types type;

    public Type(String keyword) {
        this.type = types.get(keyword);
    }
}
