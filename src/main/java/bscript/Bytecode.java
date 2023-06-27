package bscript;

import java.io.Serializable;
import java.util.List;

public record Bytecode(int version, Tree syntax, String source, List<String> args) implements Serializable {
}
