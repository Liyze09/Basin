package bscript;

import bscript.compiler.JavaStringCompiler;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;

/**
 * BScript's default compiler
 */
public final class BScriptCompiler {
    private List<String> lines = new ArrayList<>();
    private final List<String> imports = new ArrayList<>();
    private final Map<String, byte[]> compiled = new HashMap<>();

    public BScriptCompiler() {

    }

    public void addSource(String name, String src) {
        name = name.substring(0, 1).toUpperCase(Locale.ROOT) + name.substring(1);
        toJava(name, preProcess(new StringReader(src)));
    }

    @Contract(pure = true)
    public @NotNull List<String> preProcess(@NotNull Reader r) {
        List<String> lines;
        try (BufferedReader reader = new BufferedReader(r)) {
            lines = new ArrayList<>(reader.lines().toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        lines.add("endl");
        processComments(lines);
        processSweets(lines);
        processImports(lines);
        return lines;
    }


    /**
     * Generate bytecode for the bean.
     */
    public void toJava(String name, List<String> lines) {
        StringBuilder builder = new StringBuilder();
        builder.append("package bscript.classes;import bscript.BScriptEvent;import bscript.BScriptRuntime;\n");
        for (String imp : imports) {
            builder.append("import ").append(imp).append(";");
        }
        builder.append("public final class ").append(name)
                .append(" {\npublic static BScriptRuntime runtime = new BScriptRuntime();\nstatic{runtime.load(")
                .append(name).append(".class);}");
        for (String line : lines) {
            if (line.startsWith("def ")) {
                builder.append("public static ")
                        .append(line.substring(4).strip());
            } else if (line.startsWith("handle ")) {
                builder.append("public static void ")
                        .append(line.substring(7, line.lastIndexOf(" ")).strip())
                        .append("Event(BScriptEvent event){");
            } else if (line.startsWith("var ")) {
                builder.append("public static ")
                        .append(line.substring(4));
            } else if (line.startsWith("class ")) {
                builder.append("public static ")
                        .append(line);
            } else if (line.startsWith("interface ")) {
                builder.append("public ")
                        .append(line);
            } else if (line.startsWith("record ")) {
                builder.append("public ")
                        .append(line);
            } else if (line.startsWith("enum ")) {
                builder.append("public ")
                        .append(line);
            } else if (line.equals("endl;")) {
                break;
            } else {
                builder.append(line);
            }
        }
        builder.append("}");
        JavaStringCompiler.JAVAC.addSource(name, builder.toString());
    }

    public void toBytecode() {
        compiled.putAll(JavaStringCompiler.JAVAC.compile());
        JavaStringCompiler.JAVAC.clear();
    }

    @Contract(value = " -> new", pure = true)
    public @NotNull Map<String, byte[]> getCompiled() {
        return new HashMap<>(compiled);
    }

    public List<String> getLines() {
        return lines;
    }

    public void setLines(List<String> lines) {
        this.lines = lines;
    }

    private void processComments(@NotNull List<String> lines) {
        String line;
        for (int i = 0; i < lines.size(); ++i) {
            line = lines.get(i);
            if (line.isBlank() || line.startsWith("#")) {
                lines.remove(i);
                i--;
            }
        }
    }

    private void processSweets(@NotNull List<String> lines) {
        for (int i = 0; i < lines.size(); ++i) {
            String line = lines.get(i).strip();
            String nl;
            if (line.startsWith("loop")) nl = "for(;;){";
            else if (line.matches("print.*\\(.+")) nl = "System.out.println" + line.substring(line.indexOf("(")) + ";";
            else if (line.matches(".*System\\s*\\.\\s*exit.+")) nl = "runtime.close();";
            else if (line.startsWith("throw ")) nl =
                    "runtime.broadcast(\"exception\",new bscript.BScriptEvent(\"exception\","
                            + line.substring(6).strip() + "));";
            else if (line.startsWith("run ")) nl = "runtime.pool.submit(()->" + line.substring(4).strip() + ");";
            else if (!line.equals("}")) nl = line + ";";
            else nl = line;
            lines.set(i, nl);
        }
    }

    private void processImports(@NotNull List<String> lines) {
        for (int i = 0; i < lines.size(); ++i) {
            String line = lines.get(i);
            if (line.startsWith("import")) {
                imports.add(line.substring(6, line.length() - 1).strip());
                lines.remove(i);
                i--;
            }
        }
    }
}


