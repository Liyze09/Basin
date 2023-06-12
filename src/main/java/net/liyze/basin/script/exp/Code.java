package net.liyze.basin.script.exp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Code implements Serializable {
    private final List<Token> tokens = new ArrayList<>();
    public boolean addToken(Token token) {
        return this.tokens.add(token);
    }
    public List<Token> readTokens() {
        return this.tokens;
    }
}
