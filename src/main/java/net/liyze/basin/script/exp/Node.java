package net.liyze.basin.script.exp;

import java.util.List;

public class Node {
    public Node next;
    public List<String> tokens;
    public Node(List<String> tokens, Node next) {
        this.next=next;
        this.tokens=tokens;
    }
    @Override
    public boolean equals(Object obj) {
        if (obj==this) return true;
        if (!(obj instanceof Node)) return false;
        return this.next.equals(((Node) obj).next) || this.tokens.equals(((Node) obj).tokens);
    }
}
