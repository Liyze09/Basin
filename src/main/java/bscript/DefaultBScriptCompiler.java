package bscript;

import bscript.nodes.*;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

@SuppressWarnings("unused")
@ApiStatus.Experimental
public final class DefaultBScriptCompiler extends BScriptCompiler {
    public static final List<String> keywords = ImmutableList.of("(", ")", ":", "\t", " ", "\"", ">", "<", "=");


    DefaultBScriptCompiler() {
    }

    //Compile--------------------------------------------------------------------------------------------------

    @Override
    @Contract(pure = true)
    protected @NotNull List<String> preProcess(@NotNull Reader r, Path path) {
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
        return lines;
    }

    @Contract(pure = true)
    private @NotNull List<String> injectEndl(@NotNull List<String> l) {
        List<String> lines = new ArrayList<>();
        for (String line : l) {
            lines.add(line.substring(1));
        }
        lines.add("endl");
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

    @Contract(pure = true)
    private @NotNull List<String> generateTokenStream(@NotNull String line) {
        List<String> tokens = new ArrayList<>();
        var builder = new StringBuilder();
        boolean inStr = false;
        for (char c : line.toCharArray()) {
            if (c == '\"') {
                inStr = !inStr;
            }
            if (keywords.contains(String.valueOf(c)) && !inStr) {
                if (!builder.toString().isBlank()) tokens.add(builder.toString().strip());
                builder = new StringBuilder();
                if (c != ' ') tokens.add(String.valueOf(c));
                continue;
            }
            builder.append(c);
        }
        if (builder.length() != 0) tokens.add(builder.toString().strip());
        return tokens;
    }

    @Override
    protected void generateTokenStream(@NotNull List<String> lines) {
        tokenStream = new ArrayList<>(lines.size());
        for (String line : lines) {
            tokenStream.add(generateTokenStream(line));
        }
    }

    @Override
    protected void generateSyntaxTree() {
        Deque<String> nested = new ArrayDeque<>();
        Deque<Integer> nested0 = new ArrayDeque<>();
        Deque<List<String>> rts = new ArrayDeque<>();
        Element fn = new EntryNode();
        int dnd = 0;
        int id = 0;
        for (List<String> line : tokenStream) {
            LOGGER.info(line.toString());
            if (fn != null) {
                fn.next = id++;
                syntaxTree.tree.put(id, fn);
            }
            String m = line.get(0);
            switch (m) {
                case "if" -> {
                    nested.push(m);
                    nested0.push(id + 2);
                    id++;
                    rts.push(line.subList(1, line.lastIndexOf(":")));
                    fn = null;
                }
                case "loop" -> {
                    nested.push(m);
                    nested0.push(id + 2);
                    id++;
                    fn = null;
                }
                case "endl" -> {
                    m = nested.pop();
                    switch (m) {
                        case "if" -> {
                            fn = null;
                            syntaxTree.tree.put(nested0.pop() - 1, new ConditionNode(rts.pop(), id + 1));
                            syntaxTree.tree.put(++id, new OperationNode(Operation.ENDL));
                        }
                        case "loop" -> {
                            fn = null;
                            syntaxTree.tree.put(nested0.pop() - 1, new LoopNode(id + 1));
                            syntaxTree.tree.put(++id, new OperationNode(Operation.ENDL));
                        }
                        default -> throw new IllegalStateException("Unexpected value: " + m);
                    }
                }
                case "break" -> fn = new OperationNode(Operation.BREAK);
                case "ignored" -> fn = null;
                default -> {
                    if (line.size() == 1) fn = new DefaultNode(m, null);
                    else fn = new DefaultNode(m, line.subList(1, line.size()));
                }
            }
        }
    }

    //Overrides-----------------------------------------------------------------------------------------
    @Override
    public boolean equals(Object obj) {
        boolean result;
        if (!(obj instanceof DefaultBScriptCompiler)) {
            result = false;
        } else if (obj == this) {
            result = true;
        } else {
            result = this.source.equals(((DefaultBScriptCompiler) obj).source) || this.syntaxTree.equals(((DefaultBScriptCompiler) obj).syntaxTree);
        }
        return result;
    }

}
