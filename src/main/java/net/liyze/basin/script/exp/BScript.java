package net.liyze.basin.script.exp;

import net.liyze.basin.script.exp.exception.ByteCodeLoadingException;
import org.apache.commons.collections4.list.UnmodifiableList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.nustaq.serialization.FSTConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class BScript implements Serializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(BScript.class);
    @Serial
    private static final long serialVersionUID = 1024L;
    @SuppressWarnings("ConstantValue")
    public static BScript fromByteCode(byte[] bytes) throws ByteCodeLoadingException {
        var conf = FSTConfiguration.createDefaultConfiguration();
        var bs = (BScript) conf.asObject(bytes);
        if (bs.byteCodeVersion > bcv) {
            throw new ByteCodeLoadingException("Bytecode version is too high!");
        }
        return bs;
    }

    public static List<String> keywords = new UnmodifiableList<>(List.of("(", ")", ":", "\t", " ", "\""));
    public static final int bcv = 0;
    //Fields-------------------------------------------------------------------------------------------------
    public final int byteCodeVersion = bcv;
    public String source;
    public boolean withSource = true;
    public transient List<List<String>> tokenStream;
    public Queue<Node> syntaxTree = new ConcurrentLinkedDeque<>();
    //Constructor----------------------------------------------------------------------------------------------
    public BScript() {
    }
    public BScript(String s){
        this.source=s;
    }
    //Compile--------------------------------------------------------------------------------------------------
    public byte @NotNull [] toByteCode() {
        var conf = FSTConfiguration.createDefaultConfiguration();
        return conf.asByteArray(this);
    }

    protected List<String> preProcess(@NotNull Reader r, Path path) throws IOException {
        BufferedReader reader = new BufferedReader(r);
        List<String> rawLines = reader.lines().toList();
        reader.close();
        {
            List<String> lines = new java.util.ArrayList<>(rawLines);
            String line;
            for (int i = 0; i < lines.size(); ++i) {
                line = lines.get(i);
                if (line.isBlank() || line.startsWith("#")) {
                    lines.remove(i);
                    i--;
                } else if (line.startsWith("@")) {
                    if (line.startsWith("@Include")) {
                        String lp = path + line.substring(9).strip();
                        List<String> nl = this.preProcess(new FileReader(lp, StandardCharsets.UTF_8), Path.of(lp));
                        lines.addAll(0, nl);
                        i+=nl.size();
                    }
                }
            }
            return lines;
        }
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

    protected void generateTokenStream(@NotNull List<String> lines) {
        tokenStream = new ArrayList<>(lines.size());
        for (String line : lines) {
            tokenStream.add(generateTokenStream(line));
        }
    }

    public void compile() throws IOException {
        generateTokenStream(preProcess(new StringReader(source),Path.of("data/home")));
    }

    //Runtime-----------------------------------------------------------------------------------------
    protected final transient Map<String, String> stringVars = new ConcurrentHashMap<>();
    protected final transient Map<String, Character> charVars = new ConcurrentHashMap<>();
    protected final transient Map<String, Integer> intVars = new ConcurrentHashMap<>();
    protected final transient Map<String, Long> longVars = new ConcurrentHashMap<>();
    protected final transient Map<String, Short> shortVars = new ConcurrentHashMap<>();
    protected final transient Map<String, Float> floatVars = new ConcurrentHashMap<>();
    protected final transient Map<String, Double> doubleVars = new ConcurrentHashMap<>();
    protected final transient Map<String, Boolean> boolVars = new ConcurrentHashMap<>();
    //Override-----------------------------------------------------------------------------------------
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BScript)) return false;
        if (obj==this) return true;
        return this.source.equals(((BScript) obj).source) || this.syntaxTree.equals(((BScript) obj).syntaxTree);
    }
}
