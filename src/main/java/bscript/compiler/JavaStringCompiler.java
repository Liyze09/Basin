package bscript.compiler;

import bscript.exception.CompilationFailedException;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class JavaStringCompiler {

    private final JavaCompiler compiler;
    private final StandardJavaFileManager stdManager;
    public static final JavaStringCompiler JAVAC = new JavaStringCompiler();

    private JavaStringCompiler() {
        this.compiler = ToolProvider.getSystemJavaCompiler();
        this.stdManager = compiler.getStandardFileManager(null, null, null);
    }

    /**
     * Compile a Java source file in memory.
     *
     * @param className Java class name, e.g. "Test"
     * @param source    The source code as String.
     * @return The compiled results as Map that contains class name as key,
     * class binary as value.
     */
    public Map<String, byte[]> compile(String className, String source) {
        try (MemoryJavaFileManager manager = new MemoryJavaFileManager(stdManager)) {
            JavaFileObject javaFileObject = manager.makeStringSource(className + ".java", source);
            JavaCompiler.CompilationTask task = compiler.getTask(null, manager, null, List.of("--release", "17"), null, Collections.singletonList(javaFileObject));
            Boolean result = task.call();
            if (result == null || !result) {
                throw new CompilationFailedException();
            }
            return manager.getClassBytes();
        }
    }
}
