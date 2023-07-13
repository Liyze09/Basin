package bscript.test;

import bscript.BScriptHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("BScript Test 1")
public final class ScriptTest {
    @Test
    public void script() {
        BScriptHelper.getInstance().interpret("Test", """
                def void fun() {
                 print("f")
                }
                handle main {
                 run fun()
                 throw new RuntimeException()
                }
                handle around {
                 print("a")
                }
                """);
    }
}
