package bscript;

import java.io.Serializable;
import java.util.List;

public class Bytecode implements Serializable {
  public Bytecode() {
    
  }
  public Bytecode(int version, Tree syntax, String source, List<String> args) {
    this.version=version;
    this.syntax=syntax;
    this.source=source;
    this.args=args;
  }
  public int version;
  public Tree syntax;
  public String source;
  public List<String> args;
}
