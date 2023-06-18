package bscript;

import bscript.nodes.Tree;

import java.io.Serializable;

public record Bytecode(int version, Tree syntax, String source) implements Serializable {
}
