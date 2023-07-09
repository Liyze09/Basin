package bscript;

import javassist.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javassist.CtClass.voidType;

public final class BScriptCompiler {
    private final String name;
    private CtClass clazz;
    private List<String> lines = new ArrayList<>();

    public BScriptCompiler(String name) {
        this.name = name;
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
            if (line.startsWith("loop")) nl = "while(true){";
            else if (line.startsWith("print")) nl = "System.out.println" + line.substring(line.indexOf("(")) + ";";
            else if (line.matches("System\\s*\\.\\s*exit.+")) nl = "return;";
            else if (!line.equals("}")) nl = line + ";";
            else nl = line;
            lines.set(i, nl);
        }
    }

    private void processImports(@NotNull List<String> lines) {
        Map<String, String> imports = new HashMap<>();
        for (int i = 0; i < lines.size(); ++i) {
            String line = lines.get(i);
            if (line.startsWith("import")) {
                String full = line.substring(6).strip();
                imports.put(full.substring(full.lastIndexOf(".") + 1), full);
                lines.remove(i);
                i--;
            } else if (!line.startsWith("handle")) {
                int finalI = i;
                imports.forEach((key, value) -> lines.set(finalI, line.replaceAll(key, value)));
            }
        }
    }

    public void toBytecode() throws CannotCompileException {
        ClassPool pool = ClassPool.getDefault();
        CtClass clazz = pool.makeClass("bscript.classes." + getName());
        try {
            clazz.setSuperclass(pool.get("bscript.OutputBytecode"));
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
        boolean inFunc = false;
        String event = null;
        StringBuilder body = new StringBuilder();
        for (String line : getLines()) {
            if (line.startsWith("handle")) {
                inFunc = true;
                addMethod(clazz, body.toString(), event);
                event = line.substring(line.indexOf(" "), line.indexOf("{")).strip();
                body = new StringBuilder();
            } else if (line.equals("endl;")) {
                addMethod(clazz, body.toString(), event);
                break;
            } else if (line.startsWith("var")) {
                inFunc = false;
                addMethod(clazz, body.toString(), event);

            } else if (inFunc) {
                body.append(line);
            }
        }
        this.setClazz(clazz);
    }

    private void addMethod(CtClass clazz, @NotNull String body, String event) throws CannotCompileException {
        if (!body.isEmpty()) {
            CtMethod method = new CtMethod(voidType, event, new CtClass[]{}, clazz);
            method.setBody("{" + body);
            clazz.addMethod(method);
        }
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

