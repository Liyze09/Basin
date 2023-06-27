package bscript;

import bscript.nodes.DefaultNode;
import bscript.nodes.Element;

import java.util.Map;

public class DefaultBScriptRuntime extends BScriptRuntime {
    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    @Override
    public void invoke() {
        for (Map.Entry<Integer, Element> entry : syntaxTree.tree.entrySet()) {
            Element value = entry.getValue();
            if (value instanceof DefaultNode) {
                switch (((DefaultNode) value).head) {
                    case "print" -> System.out.println(((DefaultNode) value).body.get(1)
                            .substring(6, ((DefaultNode) value).body.get(1).length() - 1));
                }
            }
        }
    }
}
