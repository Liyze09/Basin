package bscript.nodes;

public class LoopNode extends UncertainNode {
    public LoopNode(int ANode) {
        super.ANode = ANode;
    }

    public String toString() {
        return "loop A: " + ANode;
    }
}
