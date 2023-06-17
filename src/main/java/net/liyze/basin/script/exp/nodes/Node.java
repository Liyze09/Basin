package net.liyze.basin.script.exp.nodes;

public class Node extends Element {
    @Override
    public boolean equals(Object obj) {
        if (obj==this) return true;
        if (!(obj instanceof Node)) return false;
        return this.next.equals(((Node) obj).next);
    }
}
