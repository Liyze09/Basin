package bscript.nodes;

import java.util.List;

public final class ConditionNode extends UncertainNode {
    final List<String> tokens;

    public ConditionNode(List<String> tokens, int ANode) {
        this.tokens = tokens;
        super.ANode = ANode;
    }

    public String toString() {
        return "A: " + ANode + " " + tokens;
    }
}
