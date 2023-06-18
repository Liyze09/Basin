package bscript.nodes;

public abstract class Element {
    public Element f;
    @Override
    public boolean equals(Object obj) {
        if (obj==this) return true;
        if (!(obj instanceof Node)) return false;
        return this.f.equals(((Element) obj).f);
    }
}
