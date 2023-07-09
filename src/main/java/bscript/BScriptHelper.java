package bscript;

import bscript.exception.LoadFailedException;
import javassist.CannotCompileException;

import java.io.IOException;
import java.io.StringReader;

public final class BScriptHelper {
    private static final BScriptHelper singleton = new BScriptHelper();

    private BScriptHelper() {
    }

    public static BScriptHelper getInstance() {
        return singleton;
    }

    public byte[] compile(String name, String source) throws CannotCompileException, IOException {
        var compiler = new BScriptCompiler(name);
        compiler.setLines(compiler.preProcess(new StringReader(source)));
        compiler.toBytecode();
        return compiler.getClazz().toBytecode();
    }

    public void compileAndRun(String name, String source) throws CannotCompileException, IOException {
        execute(compile(name, source), name);
    }

    public void execute(byte[] bytes, String name) throws LoadFailedException {
        OutputBytecode bytecode;
        try {
            bytecode = (OutputBytecode) new BScriptClassLoader(bytes).loadClass("bscript.classes." + name).getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new LoadFailedException(e);
        }
        bytecode.runtime.run();
    }
}
