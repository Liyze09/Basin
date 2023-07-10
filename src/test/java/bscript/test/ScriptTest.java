package bscript.test;

import bscript.BScriptHelper;
import javassist.CannotCompileException;
import javassist.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

@DisplayName("BScript Test 1")
public final class ScriptTest {
    @Test
    public void script() throws CannotCompileException, IOException, NotFoundException {
        BScriptHelper.getInstance().interpret("Test", """
                def void fun() {
                 print("f")
                }
                handle main {
                 fun()
                 throw new RuntimeException()
                }
                handle around {
                 print("a")
                }
                """);
    }
}
