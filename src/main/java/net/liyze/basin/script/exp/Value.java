package net.liyze.basin.script.exp;

public class Value extends Token {
    public Name name;
    public Types type;

    public Value(Name name, Types type) {
        this.name = name;
        this.type = type;
    }
}
