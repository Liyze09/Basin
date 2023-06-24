package bscript.test;

import bscript.DefaultBScriptCompiler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("BScript Test 1")
public final class ScriptTest{
    @Test
    public void scriptTest() {
        var bs = DefaultBScriptCompiler.fromSource(
                """
                        print("start")
                        var i=0
                        loop:
                        \ti=i+1
                        \tprint(i)
                        \tif(i>10):
                        \t\tprint("stopping")
                        \t\tbreak
                        print("finished")
                        """);
        bs.compile();
        bs.printTokenStream();
        bs.printSyntaxTree();
    }
}
