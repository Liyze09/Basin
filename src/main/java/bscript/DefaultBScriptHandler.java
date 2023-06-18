package bscript;

import bscript.nodes.*;
import org.apache.commons.collections4.list.UnmodifiableList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.nustaq.serialization.FSTConfiguration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

@SuppressWarnings("unused")
@ApiStatus.Experimental
public final class DefaultBScriptHandler extends BScriptHandler {
    public static List<String> keywords = new UnmodifiableList<>(List.of("(", ")", ":", "\t", " ", "\"", ">", "<", "="));
    static final ThreadLocal<FSTConfiguration> conf = ThreadLocal.withInitial(() -> {
        FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
        conf.registerClass(DefaultBScriptHandler.class);
        return conf;
    });

    DefaultBScriptHandler() {
    }

    //Compile--------------------------------------------------------------------------------------------------

    @Override
    @Contract(pure = true)
    protected @NotNull List<String> preProcess(@NotNull Reader r, Path path) {
        List<String> lines;
        try(BufferedReader reader = new BufferedReader(r)) {
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
                        i+=nl.size();
                    }
                }
            }
        }
        //Part 2 -- process tabs.
        {
            int in = 0;
            List<Integer> indexes = new ArrayList<>();
            boolean added = false;
            for (int i=0;i< lines.size();++i) {
                String line = lines.get(i);
                if (line.startsWith("\t") && !added) {
                    indexes.add(i);
                    added = true;
                } else if (!line.startsWith("\t")){
                    added = false;
                }
            }
            for (int i : indexes) {
                List<String> inLines = new ArrayList<>();
                for (int j = i;;++j) {
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
        for (String line : l){
            lines.add(line.substring(1));
        }
        lines.add("endl");
        int in = 0;
        List<Integer> indexes = new ArrayList<>();
        boolean added = false;
        for (int i=0;i < lines.size();++i) {
            String line = lines.get(i);
            if (line.startsWith("\t") && !added) {
                indexes.add(i);
                added = true;
            } else if (!line.startsWith("\t")){
                added = false;
            }
        }
        for (int i : indexes) {
            List<String> inLines = new ArrayList<>();
            for (int j = i;;++j) {
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
                if (!builder.toString().isBlank())tokens.add(builder.toString().strip());
                builder = new StringBuilder();
                if (c != ' ') tokens.add(String.valueOf(c));
                continue;
            }
            builder.append(c);
        }
        if (builder.length()!=0) tokens.add(builder.toString().strip());
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
        Deque<Element> nested1 = new ArrayDeque<>();
        Deque<List<String>> rts = new ArrayDeque<>();
        Element fn = new EntryNode();
        int dnd = 0;
        int id = 0;
        for (List<String> line : tokenStream) {
            syntaxTree.tree.put(id++,fn);
            String m = line.get(0);

        }
    }
    //Runtime-----------------------------------------------------------------------------------------

    //Overrides-----------------------------------------------------------------------------------------
    @Override
    public boolean equals(Object obj) {
        boolean result;
        if (!(obj instanceof DefaultBScriptHandler)) {
            result = false;
        } else if (obj == this) {
            result = true;
        } else {
            result = this.source.equals(((DefaultBScriptHandler) obj).source) || this.syntaxTree.equals(((DefaultBScriptHandler) obj).syntaxTree);
        }
        return result;
    }

    @Override
    public int hashCode(){
        return Arrays.hashCode(toByteCode());
    }
}
