package bscript.nodes;

import bscript.Operation;

public class OperationNode extends Node{
    public Operation operation;
    public OperationNode(Operation operation) {
        this.operation = operation;
    }

    @Override
    public String toString() {
        return operation.name();
    }
}
