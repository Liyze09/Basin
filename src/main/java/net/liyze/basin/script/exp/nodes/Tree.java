package net.liyze.basin.script.exp.nodes;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Tree extends Element {
    public Queue<Element> tree = new ConcurrentLinkedQueue<>();
    @Override
    public int hashCode(){
        return tree.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        boolean result;
        if (!(obj instanceof Tree)) {
            result = false;
        } else if (obj == this) {
            result = true;
        } else {
            result = this.tree.equals(((Tree) obj).tree);
        }
        return result;
    }
}
