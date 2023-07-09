package bscript;

import javassist.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static javassist.CtClass.voidType;

public final class BScriptCompiler {
    private final String name;
    private CtClass clazz;
    private List<String> lines = new ArrayList<>();

    public BScriptCompiler(String name) {
        this.name = name;
    }

    @Contract(pure = true)
    public @NotNull List<String> preProcess(@NotNull Reader r, Path path) {
        List<String> lines;
        try (BufferedReader reader = new BufferedReader(r)) {
            lines = new ArrayList<>(reader.lines().toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        lines.add("ignored");
        //Part 1 -- process Notes and Annotations.
        {
            String line;
            for (int i = 0; i < lines.size(); ++i) {
                line = lines.get(i);
                if (line.isBlank() || line.startsWith("#")) {
                    lines.remove(i);
                    i--;
                } else if (line.startsWith("@")) {
                    if (line.startsWith("@Include")) {
                        String lp = path + line.substring(9).strip();
                        List<String> nl;
                        try {
                            nl = this.preProcess(new FileReader(lp, StandardCharsets.UTF_8), Path.of(lp));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        lines.addAll(0, nl);
                        i += nl.size();
                    }
                }
            }
        }
        //Part 2 -- process tabs.
        {
            int in = 0;
            List<Integer> indexes = new ArrayList<>();
            boolean added = false;
            for (int i = 0; i < lines.size(); ++i) {
                String line = lines.get(i);
                if (line.startsWith("\t") && !added) {
                    indexes.add(i);
                    added = true;
                } else if (!line.startsWith("\t")) {
                    added = false;
                }
            }
            for (int i : indexes) {
                List<String> inLines = new ArrayList<>();
                for (int j = i; ; ++j) {
                    String line = lines.get(j);
                    if (!line.startsWith("\t")) {
                        break;
                    } else {
                        inLines.add(line);
                    }
                }
                lines.removeAll(inLines);
                lines.addAll(i, injectEndl(inLines));
            }
        }
        //Part 3 -- to Java code
        {
            for (int i = 0; i < lines.size(); ++i) {
                String line = lines.get(i).strip();
                String nl;
                if (line.matches("loop\\s*:.*")) nl = "while(true){";
                else if (line.endsWith(":")) nl = line.substring(0, line.length() - 1) + "{";
                else if (line.startsWith("print")) nl = "System.out.println" + line.substring(line.indexOf("(")) + ";";
                else if (!line.equals("}")) nl = line + ";";
                else nl = line;
                lines.set(i, nl);
            }
        }
        return lines;
    }

    @Contract(pure = true)
    private @NotNull List<String> injectEndl(@NotNull List<String> l) {
        List<String> lines = new ArrayList<>();
        for (String line : l) {
            lines.add(line.substring(1));
        }
        lines.add("}");
        int in = 0;
        List<Integer> indexes = new ArrayList<>();
        boolean added = false;
        for (int i = 0; i < lines.size(); ++i) {
            String line = lines.get(i);
            if (line.startsWith("\t") && !added) {
                indexes.add(i);
                added = true;
            } else if (!line.startsWith("\t")) {
                added = false;
            }
        }
        for (int i : indexes) {
            List<String> inLines = new ArrayList<>();
            for (int j = i; ; ++j) {
                String line = lines.get(j);
                if (!line.startsWith("\t")) {
                    break;
                } else {
                    inLines.add(line);
                }
            }
            lines.removeAll(inLines);
            lines.addAll(i, injectEndl(inLines));
        }
        return lines;
    }

    public void toBytecode() throws CannotCompileException {
        ClassPool pool = ClassPool.getDefault();
        CtClass clazz = pool.makeClass("bscript.classes." + getName());
        try {
            clazz.setSuperclass(pool.get("bscript.OutputBytecode"));
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
        String event = null;
        StringBuilder body = new StringBuilder();
        for (String line : getLines()) {
            if (line.startsWith("handle")) {
                if (!body.isEmpty()) {
                    CtMethod method = new CtMethod(voidType, event, new CtClass[]{}, clazz);
                    method.setBody("{" + body);
                    clazz.addMethod(method);
                }
                event = line.substring(line.indexOf(" "), line.indexOf("{")).strip();
                body = new StringBuilder();
            } else if (line.equals("ignored;")) {
                if (!body.isEmpty()) {
                    CtMethod method = new CtMethod(voidType, event, new CtClass[]{}, clazz);
                    method.setBody("{" + body);
                    clazz.addMethod(method);
                }
            } else {
                body.append(line);
            }
        }
        this.setClazz(clazz);
    }

    public String getName() {
        return name;
    }

    public CtClass getClazz() {
        return clazz;
    }

    public void setClazz(CtClass clazz) {
        this.clazz = clazz;
    }

    public List<String> getLines() {
        return lines;
    }

    public void setLines(List<String> lines) {
        this.lines = lines;
    }
}

