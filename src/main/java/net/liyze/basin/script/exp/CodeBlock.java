package net.liyze.basin.script.exp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class CodeBlock implements Serializable {
    public String name;
    protected final transient Map<String, String> stringVars;
    protected final transient Map<String, Character> charVars;
    protected final transient Map<String, Integer> intVars;
    protected final transient Map<String, Long> longVars;
    protected final transient Map<String, Short> shortVars;
    protected final transient Map<String, Float> floatVars;
    protected final transient Map<String, Boolean> boolVars;
    protected final transient Map<String, Double> doubleVars;
    public Types returnType;
    public Queue<ArrayList<Token>> code;

    public CodeBlock(Queue<ArrayList<Token>> code, List<Token> tokens, ExpParser parent) {
        this.code = code;
        this.returnType = ((Type) tokens.get(1)).type;
        this.name = tokens.get(2).name;
        this.boolVars = parent.boolVars;
        this.floatVars = parent.floatVars;
        this.doubleVars = parent.doubleVars;
        this.shortVars = parent.shortVars;
        this.charVars = parent.charVars;
        this.intVars = parent.intVars;
        this.stringVars = parent.stringVars;
        this.longVars = parent.longVars;
    }
}
