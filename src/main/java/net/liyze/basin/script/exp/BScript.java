package net.liyze.basin.script.exp;

import net.liyze.basin.script.exp.exception.ByteCodeLoadingException;
import net.liyze.basin.script.exp.nodes.Tree;
import org.apache.commons.collections4.list.UnmodifiableList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.nustaq.serialization.FSTConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
@ApiStatus.Experimental
public class BScript implements Serializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(BScript.class);
    @Serial
    private static final long serialVersionUID = 1024L;
    public static List<String> keywords = new UnmodifiableList<>(List.of("(", ")", ":", "\t", " ", "\"", ">", "<", "="));
    public static final int bcv = 0;
    private static final ThreadLocal<FSTConfiguration> conf = ThreadLocal.withInitial(() -> {
        FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
        conf.registerClass(BScript.class);
        return conf;
    });
    //Fields-------------------------------------------------------------------------------------------------
    private int byteCodeVersion = bcv;
    public String source;
    private boolean withSource = false;
    public transient List<List<String>> tokenStream;
    public Tree syntaxTree = new Tree();
    //Static-Factories----------------------------------------------------------------------------------------------
    private BScript() {
    }
    @Contract(pure = true)
    public static @NotNull BScript fromByteCode(byte[] bytes) throws ByteCodeLoadingException {
        var bs = (BScript) conf.get().asObject(bytes);
        if (bs.getByteCodeVersion() > bcv) {
            throw new ByteCodeLoadingException("Bytecode version is too high! Please use higher BScript");
        }
        LOGGER.info("Generated a new BS handler with bytecode.");
        return bs;
    }
    @Contract(pure = true)
    public static @NotNull BScript fromInputStream(@NotNull InputStream stream) throws ByteCodeLoadingException, IOException {
        byte[] bytes = stream.readAllBytes();
        var bs = (BScript) conf.get().asObject(bytes);
        if (bs.getByteCodeVersion() > bcv) {
            throw new ByteCodeLoadingException("Bytecode version is too high! Please use higher BScript");
        }
        LOGGER.info("Generated a new BS handler with bytecode stream.");
        return bs;
    }
    @Contract(pure = true)
    public static @NotNull BScript fromSource(String source) {
        var bs = new BScript();
        bs.source=source;
        LOGGER.info("Generated a new BS handler with source string.");
        return bs;
    }
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Contract(pure = true)
    public static @NotNull BScript fromReader(@NotNull Reader reader) throws IOException {
        var bs = new BScript();
        char[] chars = new char[0];
        reader.read(chars);
        bs.source=new String(chars);
        LOGGER.info("Generated a new BS handler with source reader.");
        reader.close();
        return bs;
    }
    //Compile--------------------------------------------------------------------------------------------------
    @Contract(pure = true)
    public byte @NotNull [] toByteCode() {
        if (!isWithSource()) this.source = "#";
        LOGGER.info("Saving bytecode.");
        return conf.get().asByteArray(this);
    }
    @Contract(pure = true)
    protected List<String> preProcess(@NotNull Reader r, Path path) throws IOException {
        BufferedReader reader = new BufferedReader(r);
        List<String> lines = new ArrayList<>(reader.lines().toList());
        lines.add("ignored");
        reader.close();
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
                        List<String> nl = this.preProcess(new FileReader(lp, StandardCharsets.UTF_8), Path.of(lp));
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

    protected void generateTokenStream(@NotNull List<String> lines) {
        tokenStream = new ArrayList<>(lines.size());
        for (String line : lines) {
            tokenStream.add(generateTokenStream(line));
        }
    }

    public void compile() {
            try {
                generateTokenStream(preProcess(new StringReader(source), Path.of("data/home")));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            generateSyntaxTree();
    }
    public void generateSyntaxTree() {
        List<String> rt = new ArrayList<>();
        Deque<String> nested = new ArrayDeque<>();
        for (List<String> line:tokenStream) {
            String m = line.get(0);
            if (m.equals("if")) {
                rt = line.subList(2, line.lastIndexOf(")"));
                nested.addLast(m);
            }
            if (m.equals("endl")) {
                String last = nested.getLast();

            }
        }
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
    //Overrides-----------------------------------------------------------------------------------------
    @Override
    public boolean equals(Object obj) {
        boolean result;
        if (!(obj instanceof BScript)) {
            result = false;
        } else if (obj == this) {
            result = true;
        } else {
            result = this.source.equals(((BScript) obj).source) || this.syntaxTree.equals(((BScript) obj).syntaxTree);
        }
        return result;
    }

    @Override
    public int hashCode(){
        return Arrays.hashCode(toByteCode());
    }
    //Bean-Methods---------------------------------------------------------------------------------------
    public int getByteCodeVersion() {
        return byteCodeVersion;
    }

    public void setByteCodeVersion(int byteCodeVersion) {
        this.byteCodeVersion = byteCodeVersion;
    }

    public boolean isWithSource() {
        return withSource;
    }

    public void setWithSource(boolean withSource) {
        this.withSource = withSource;
    }
}
