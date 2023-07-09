package bscript.test;

import bscript.BScriptHelper;
import javassist.CannotCompileException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

@DisplayName("BScript Test 1")
public final class ScriptTest {

    @Test
    public void bct() throws CannotCompileException, IOException {
        BScriptHelper.getInstance().compileAndRun("Test",
                """
                        handle main:
                        \tprint("start")
                        \tint i=0
                        \tloop:
                        \t\ti=i+1
                        \t\tprint(i)
                        \t\tif(i>=10):
                        \t\t\tprint("stopping")
                        \t\t\tbreak
                        \tprint("finished")
                        """);
    }
}
