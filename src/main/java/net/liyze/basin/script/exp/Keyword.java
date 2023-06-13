package net.liyze.basin.script.exp;

import static net.liyze.basin.script.exp.ExpParser.keywords;

public class Keyword extends Token {
    public Keywords keyword;

    public Keyword(String keyword) {
        this.keyword = keywords.get(keyword);
    }
}
