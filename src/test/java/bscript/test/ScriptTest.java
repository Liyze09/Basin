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
                        print("start")
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
