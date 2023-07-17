package bscript.compiler;

import bscript.exception.CompilationFailedException;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class JavaStringCompiler {

    private final JavaCompiler compiler;
    private final StandardJavaFileManager stdManager;
    public static final JavaStringCompiler JAVAC = new JavaStringCompiler();

    private JavaStringCompiler() {
        this.compiler = ToolProvider.getSystemJavaCompiler();
        this.stdManager = compiler.getStandardFileManager(null, null, null);
    }

    private final Map<String, String> src = new HashMap<>();

    /**
     * Add a source String to compiler
     *
     * @param className Java class name, e.g. "Test"
     * @param source    The source code as String.
     */
    public void addSource(String className, String source) {
        src.put(className, source);
    }

    /**
     * Compile Java sources in memory.
     *
     * @return The compiled results as Map that contains class name as key,
     * class binary as value.
     */
    public Map<String, byte[]> compile() {
        try (MemoryJavaFileManager manager = new MemoryJavaFileManager(stdManager)) {
            final List<JavaFileObject> fileObjects = new ArrayList<>();
            src.forEach((n, s) -> {
                JavaFileObject javaFileObject = manager.makeStringSource(n + ".java", s);
                fileObjects.add(javaFileObject);
            });
            JavaCompiler.CompilationTask task = compiler.getTask(null, manager, null, List.of("--release", "17"), null, fileObjects);
            Boolean result = task.call();
            if (result == null || !result) {
                throw new CompilationFailedException();
            }
            return manager.getClassBytes();
        }
    }

    public Map<String, String> getSource() {
        return src;
    }

    public void clear() {
        src.clear();
    }
}
