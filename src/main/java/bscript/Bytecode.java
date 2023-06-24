package bscript;

import java.io.Serializable;

public record Bytecode(int version, Tree syntax, String source) implements Serializable {
}
