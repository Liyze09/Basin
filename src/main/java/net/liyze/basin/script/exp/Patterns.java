package net.liyze.basin.script.exp;

import org.apache.commons.collections4.list.UnmodifiableList;

import java.util.List;

public enum Patterns {
    LK("("),
    RK(")"),
    LD("{"),
    RD("}"),
    DH(","),
    LZ("["),
    RZ("]"),
    GH(">"),
    GE(">="),
    EH("=="),
    LE("<="),
    LH("<"),
    FZ("="),
    CH("*"),
    AH("+"),
    JH("-"),
    XH("/"),
    ZH("//"),
    QY("%"),
    PH("^"),
    AD("&&"),
    OR("||"),
    NT("!"),
    MH(":"),
    SJ("\t");
    public static final List<Character> words = new UnmodifiableList<>(List.of('(', ')', '[', ']', '{', '}', ',', '=', '<', '>', '+', '-', '*', '/', '%', '^', '&', '|', '!', ':', ' ', '\t'));

    Patterns(String s) {
    }
}
