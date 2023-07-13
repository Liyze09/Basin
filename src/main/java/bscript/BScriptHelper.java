package bscript;

import bscript.exception.LoadFailedException;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

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
     */
    public Map<String, byte[]> compile(String name, String source) {
        var compiler = new BScriptCompiler(name);
        compiler.setLines(compiler.preProcess(new StringReader(source)));
        compiler.toBytecode();
        return compiler.getCompiled();
    }

    /**
     * Compile source string to JVM bytecode and write into a .class file
     *
     * @param name   The name of the BScript Bean
     * @param source Bean's all source
     * @param dir    The directory of the .class file
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void compileToFile(String name, String source, String dir) throws IOException {
        var compiler = new BScriptCompiler(name);
        compiler.setLines(compiler.preProcess(new StringReader(source)));
        compiler.toBytecode();
        for (Map.Entry<String, byte[]> entry : compiler.getCompiled().entrySet()) {
            var file = new File(dir + entry.getKey() + ".class");
            file.mkdirs();
            file.createNewFile();
            try (var stream = new FileOutputStream(file)) {
                stream.write(entry.getValue());
            }
        }
    }

    /**
     * Compile a .bs file to JVM bytecode and write into a .class file in the same directory
     *
     * @param source The .bs file
     */
    public void compileFile(@NotNull File source) throws IOException {
        String name = source.getName();
        try (InputStream inputStream = new FileInputStream(source)) {
            compileToFile(name.substring(0, name.length() - 3), new String(inputStream.readAllBytes(), StandardCharsets.UTF_8), source.getParent());
        }
    }

    /**
     * Compile a .bs file to JVM bytecode and execute it
     *
     * @param source The .bs file
     */
    public void interpretFile(@NotNull File source) throws IOException {
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
     */
    public void interpret(String name, String source) {
        execute(compile(name, source), name);
    }

    /**
     * Execute a compiled BScript Bean
     *
     * @param bytes All data of the class
     * @param name  The full name of the class
     * @throws LoadFailedException If class can't load to JVM.
     */
    public void execute(Map<String, byte[]> bytes, String name) throws LoadFailedException {
        OutputBytecode bytecode;
        try (URLClassLoader loader = new BScriptClassLoader(bytes)) {
            bytecode = (OutputBytecode) loader.loadClass("bscript.classes." + name).getDeclaredConstructor().newInstance();
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
            execute(Map.of(name, stream.readAllBytes()), name.substring(0, name.length() - 6));
        }
    }
}
