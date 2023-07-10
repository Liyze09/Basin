package bscript;

import bscript.exception.LoadFailedException;
import javassist.CannotCompileException;
import javassist.NotFoundException;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * The util class of BScript
 */
public final class BScriptHelper {
    private static final BScriptHelper singleton = new BScriptHelper();

    private BScriptHelper() {
    }

    /**
     * Get the singleton of BScriptHelper
     * @return The singleton of BScriptHelper
     */
    public static BScriptHelper getInstance() {
        return singleton;
    }

    /**
     * Compile source string to JVM bytecode
     *
     * @param name   The name of the BScript Bean
     * @param source Bean's all source
     * @return Outputted JVM bytecode
     * @throws CannotCompileException If the source can't compile
     */
    public byte[] compile(String name, String source) throws CannotCompileException, IOException, NotFoundException {
        var compiler = new BScriptCompiler(name);
        compiler.setLines(compiler.preProcess(new StringReader(source)));
        compiler.toBytecode();
        return compiler.getClazz().toBytecode();
    }

    /**
     * Compile source string to JVM bytecode and write into a .class file
     *
     * @param name   The name of the BScript Bean
     * @param source Bean's all source
     * @param dir    The directory of the .class file
     * @throws CannotCompileException If the source can't compile
     */
    public void compileToFile(String name, String source, String dir) throws CannotCompileException, IOException, NotFoundException {
        var compiler = new BScriptCompiler(name);
        compiler.setLines(compiler.preProcess(new StringReader(source)));
        compiler.toBytecode();
        compiler.getClazz().writeFile(dir);
    }

    /**
     * Compile a .bs file to JVM bytecode and write into a .class file in the same directory
     *
     * @param source The .bs file
     * @throws CannotCompileException If the source can't compile
     */
    public void compileFile(@NotNull File source) throws CannotCompileException, IOException, NotFoundException {
        String name = source.getName();
        try (InputStream inputStream = new FileInputStream(source)) {
            compileToFile(name.substring(0, name.length() - 3), new String(inputStream.readAllBytes(), StandardCharsets.UTF_8), source.getParent());
        }
    }

    /**
     * Compile a .bs file to JVM bytecode and execute it
     *
     * @param source The .bs file
     * @throws CannotCompileException If the source can't compile
     */
    public void interpretFile(@NotNull File source) throws IOException, CannotCompileException, NotFoundException {
        String name = source.getName();
        try (InputStream inputStream = new FileInputStream(source)) {
            interpret(name.substring(0, name.length() - 3), new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    /**
     * Compile source string to JVM bytecode and execute it
     *
     * @param name   The name of the BScript Bean
     * @param source Bean's all source
     * @throws CannotCompileException If the source can't compile
     */
    public void interpret(String name, String source) throws CannotCompileException, IOException, NotFoundException {
        execute(compile(name, source), name);
    }

    /**
     * Execute a compiled BScript Bean
     * @param bytes All data of the class
     * @param name The full name of the class
     * @throws LoadFailedException If class can't load to JVM.
     */
    public void execute(byte[] bytes, String name) throws LoadFailedException {
        OutputBytecode bytecode;
        try {
            bytecode = (OutputBytecode) new BScriptClassLoader(bytes).loadClass("bscript.classes." + name).getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new LoadFailedException(e);
        }
        bytecode.runtime.run();
    }
    /**
     * Execute a compiled BScript Bean
     * @param clazz Class file
     * @throws LoadFailedException If class can't load to JVM.
     */
    public void executeFile(@NotNull File clazz) throws IOException {
        String name = clazz.getName();
        try (InputStream stream = new FileInputStream(clazz)) {
            execute(stream.readAllBytes(), name.substring(0, name.length() - 6));
        }
    }
}
