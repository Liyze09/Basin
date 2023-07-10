package bscript.test;

import bscript.BScriptHelper;
import javassist.CannotCompileException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

@DisplayName("BScript Test 1")
public final class ScriptTest {
    @Test
    public void script() throws CannotCompileException, IOException {
        BScriptHelper.getInstance().interpret("Test", """
                def void fun() {
                 System.exit(0)
                }
                handle main {
                 print("m")
                 fun()
                }
                handle before {
                 print("b")
                }
                """);
    }
}
