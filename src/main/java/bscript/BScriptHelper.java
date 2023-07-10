package bscript;

import bscript.exception.LoadFailedException;
import javassist.CannotCompileException;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;

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

    public void compileToFile(String name, String source, String dir) throws CannotCompileException, IOException {
        var compiler = new BScriptCompiler(name);
        compiler.setLines(compiler.preProcess(new StringReader(source)));
        compiler.toBytecode();
        compiler.getClazz().writeFile(dir);
    }

    public void compileFile(@NotNull File source) throws CannotCompileException, IOException {
        String name = source.getName();
        try (InputStream inputStream = new FileInputStream(source)) {
            compileToFile(name.substring(0, name.length() - 3), new String(inputStream.readAllBytes(), StandardCharsets.UTF_8), source.getParent());
        }
    }

    public void interpretFile(@NotNull File source) throws IOException, CannotCompileException {
        String name = source.getName();
        try (InputStream inputStream = new FileInputStream(source)) {
            interpret(name.substring(0, name.length() - 3), new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    public void interpret(String name, String source) throws CannotCompileException, IOException {
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

    public void executeFile(@NotNull File clazz) throws IOException {
        String name = clazz.getName();
        try (InputStream stream = new FileInputStream(clazz)) {
            execute(stream.readAllBytes(), name.substring(0, name.length() - 6));
        }
    }
}
