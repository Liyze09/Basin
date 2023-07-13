package bscript.test;

import bscript.BScriptHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

@DisplayName("BScript Test 1")
public final class ScriptTest {
    @Test
    public void script() throws IOException {
        BScriptHelper.getInstance().compileToFile("Test", """
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
                handle exception {
                }
                class Inner {
                 
                }
                """, "data/output".replace('/', File.separatorChar));
        BScriptHelper.getInstance().executeFile(new File("data/output/Test.class"));
    }
}
