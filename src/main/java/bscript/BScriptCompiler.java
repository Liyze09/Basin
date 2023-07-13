package bscript;

import bscript.compiler.JavaStringCompiler;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

/**
 * BScript's default compiler
 */
public final class BScriptCompiler {
    private final String name;
    private List<String> lines = new ArrayList<>();
    private final List<String> imports = new ArrayList<>();
    private final Map<String, byte[]> compiled = new HashMap<>();

    public BScriptCompiler(@NotNull String name) {
        this.name = name.substring(0, 1).toUpperCase(Locale.ROOT) + name.substring(1);
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

    /**
     * Generate bytecode for the bean.
     */
    public void toBytecode() {
        StringBuilder builder = new StringBuilder();
        builder.append("package bscript.classes;import bscript.BScriptEvent;import bscript.OutputBytecode;\n");
        for (String imp : imports) {
            builder.append("import ").append(imp).append(";");
        }
        builder.append("public final class ").append(name).append(" extends OutputBytecode {\n");
        for (String line : getLines()) {
            if (line.startsWith("def ")) {
                builder.append("public ")
                        .append(line.substring(4).strip());
            } else if (line.startsWith("handle ")) {
                builder.append("public void ")
                        .append(line.substring(7, line.lastIndexOf(" ")).strip())
                        .append("Event(BScriptEvent event){");
            } else if (line.startsWith("var ")) {
                builder.append("public ")
                        .append(line.substring(4));
            } else if (line.startsWith("class ")) {
                builder.append("static ")
                        .append(line);
            } else if (line.equals("endl;")) {
                break;
            } else {
                builder.append(line);
            }
        }
        builder.append("}");
        compiled.putAll(JavaStringCompiler.JAVAC.compile(name, builder.toString()));
    }

    public String getName() {
        return name;
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
}


