package bscript.nodes;

public abstract class Element {
    public Integer next;

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof Node)) return false;
        return this.next.equals(((Element) obj).next);
    }
}
