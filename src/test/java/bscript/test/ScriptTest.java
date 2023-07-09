package bscript.test;

import bscript.BScriptHelper;
import javassist.CannotCompileException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

@DisplayName("BScript Test 1")
public final class ScriptTest {

    @Test
    public void bct() throws CannotCompileException, IOException {
        BScriptHelper.getInstance().compileAndRun("Test",
                """
                        var int i = 0
                        handle main {
                        \tprint("start")
                        \tloop{
                        \t\ti=i+1
                        \t\tprint(i)
                        \t\tif(i>=10){
                        \t\t\tprint("stopping")
                        \t\t\tbreak
                        \t\t}
                        \t}
                        \tprint("finished")
                        \truntime.broadcast("test")
                        }
                        handle test{
                        \tprint("Hello, World!")
                        }
                        """);
    }

    @Test
    public void bft() throws CannotCompileException, IOException {
        BScriptHelper.getInstance().compileFile(new File("data/home/main.bs".replace('/', File.separatorChar)));
        BScriptHelper.getInstance().executeFile(new File("data/home/bscript/classes/Main.class".replace('/', File.separatorChar)));
    }

    @Test
    public void bit() throws CannotCompileException, IOException {
        BScriptHelper.getInstance().compileAndRun("Test",
                """
                         import bscript.test.ScriptTest
                         handle main {
                           ScriptTest x = new ScriptTest();
                           x.ok()
                         }
                        """);
    }

    public void ok() {
        System.out.println("well");
    }

    @Test
    public void bmt() throws CannotCompileException, IOException {
        BScriptHelper.getInstance().compileAndRun("Test",
                """
                         def void hello(String t) {
                          print("Hello, "+t)
                         }
                         handle main {
                           hello("Bob.")
                         }
                         
                        """);
    }
}
