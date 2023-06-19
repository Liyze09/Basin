package bscript.test;

import bscript.DefaultBScriptHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("BScript Test 1")
public final class ScriptTest{
    @Test
    public void scriptTest() {
        main(null);
    }

    public static void main(String[] args) {
        var bs = DefaultBScriptHandler.fromSource(
                """
                        loop:
                        \ti=i+1
                        \tif(i>10):
                        \t\tbreak
                        """);
        bs.compile();
        bs.printTokenStream();
        bs.printSyntaxTree();
    }
}
