package bscript;

import bscript.nodes.Element;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Tree implements Serializable {
    public Map<Integer, Element> tree = new ConcurrentHashMap<>();

    @Override
    public int hashCode() {
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
