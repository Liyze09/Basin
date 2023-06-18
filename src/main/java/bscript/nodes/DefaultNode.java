package bscript.nodes;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DefaultNode extends Node {
    public final String head;
    public final List<String> body;

    public DefaultNode(String head, @Nullable List<String> body) {
        this.head = head;
        this.body = body;
    }
}
