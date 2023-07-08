package bscript.test;

import bscript.BScriptClassLoader;
import bscript.BScriptCompiler;
import javassist.CannotCompileException;
import javassist.ClassPool;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;

@DisplayName("BScript Test 1")
public final class ScriptTest {

    @Test
    public void bct() throws CannotCompileException, IOException, ClassNotFoundException {
        var bs = new BScriptCompiler("Test");
        var pool = ClassPool.getDefault();
        bs.lines = bs.preProcess(new StringReader("""
                handle main:
                \tprint("start")
                \tint i=0
                \tloop:
                \t\ti=i+1
                \t\tprint(i)
                \t\tif(i>10):
                \t\t\tprint("stopping")
                \t\t\tbreak
                \tprint("finished")
                """), null);
        bs.toBytecode();
        new BScriptClassLoader(bs.clazz.toBytecode()).loadClass("bscript.classes.Test");
    }
}
