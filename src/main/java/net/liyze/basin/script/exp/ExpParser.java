package net.liyze.basin.script.exp;

import net.liyze.basin.script.CommandParser;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static net.liyze.basin.core.Main.commands;
import static net.liyze.basin.script.exp.Patterns.words;

@SuppressWarnings("unused")
public class ExpParser implements Serializable {
    @Serial
    private static final long serialVersionUID = 1024L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ExpParser.class);
    protected Queue<ArrayList<Token>> queue = new ConcurrentLinkedQueue<>();
    public static final Map<String, Keywords> keywords;
    public static final Map<String, Patterns> patterns;
    public static final Map<String, Types> types;
    public static Map<String, Function<List<String>, List<String>>> annotations = new HashMap<>();
    protected transient Thread compileThread;
    protected transient boolean compiled = false;
    protected transient boolean inCodeBlock = false;
    protected transient ExecutorService runtimeThreadPool = Executors.newCachedThreadPool();
    protected transient Thread runtimeThread;
    //Var
    protected final transient Map<String, String> stringVars = new ConcurrentHashMap<>();
    protected final transient Map<String, Character> charVars = new ConcurrentHashMap<>();
    protected final transient Map<String, Integer> intVars = new ConcurrentHashMap<>();
    protected final transient Map<String, Long> longVars = new ConcurrentHashMap<>();
    protected final transient Map<String, Short> shortVars = new ConcurrentHashMap<>();
    protected final transient Map<String, Float> floatVars = new ConcurrentHashMap<>();
    protected final transient Map<String, Double> doubleVars = new ConcurrentHashMap<>();
    protected final transient Map<String, Boolean> boolVars = new ConcurrentHashMap<>();
    protected final transient Map<String, CodeBlock> methods = new ConcurrentHashMap<>();

    static {
        Map<String, Keywords> rt1 = new HashMap<>();
        Arrays.stream(Keywords.values()).forEach(key -> rt1.put(key.name(), key));
        keywords = Collections.unmodifiableMap(rt1);
        Map<String, Patterns> rt2 = new HashMap<>();
        Arrays.stream(Patterns.values()).forEach(key -> rt2.put(key.name(), key));
        patterns = Collections.unmodifiableMap(rt2);
        Map<String, Types> rt3 = new HashMap<>();
        Arrays.stream(Types.values()).forEach(key -> rt3.put(key.name(), key));
        types = Collections.unmodifiableMap(rt3);
    }


    public ExpParser() {
    }

    public byte @Nullable [] serialize() {
        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
            objectStream.writeObject(this);
            objectStream.flush();
            byte[] bytes = byteStream.toByteArray();
            byteStream.close();
            objectStream.close();
            return bytes;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static @NotNull ExpParser loadFrom(InputStream s) throws IOException, ClassNotFoundException {
        ObjectInputStream stream = new ObjectInputStream(s);
        ExpParser parser = (ExpParser) stream.readObject();
        parser.compiled = true;
        return parser;

    }

    protected List<String> getLines(Reader r) throws IOException {
        BufferedReader reader = new BufferedReader(r);
        List<String> lines = reader.lines().toList();
        reader.close();
        return lines;
    }

    @SuppressWarnings("SuspiciousListRemoveInLoop")
    protected List<String> preProcess(@NotNull List<String> lines) throws IOException {
        {
            String line;
            for (int i = 0; i < lines.size(); ++i) {
                line = lines.get(i);
                if (line.isBlank() || line.startsWith("#")) {
                    lines.remove(i);
                } else if (line.startsWith("@")) {
                    if (line.startsWith("@Include")) {
                        lines.addAll(i, this.preProcess(this.getLines(new FileReader(
                                "data" + File.separator + "home" + line.substring(9).strip(),
                                StandardCharsets.UTF_8))));
                    } else {
                        lines = annotations.get(line).apply(lines);
                    }
                    lines.remove(i);
                }
            }
        }
        return lines;
    }

    protected List<Token> generateTokenStream(@NotNull String code) {
        List<Token> tokens = new ArrayList<>();
        var builder = new StringBuilder();
        boolean isEnd = true;
        boolean inString = false;
        // def func(string str, int int):  ->  [k.DEF, "func", p.LK, k.STRING, "str", p.DH, k.INT, "int", p.RK, p.MH]
        for (Character c : code.toCharArray()) {
            if (c == '\"') {
                tokens.add(new Name(builder.toString()));
                builder = new StringBuilder();
                inString = !inString;
            } else if (c.equals(' ')) {
                tokens.add(new Name(builder.toString()));
                builder = new StringBuilder();
            }
            if (!words.contains(c)) {
                builder.append(c);
            } else if (!inString) {
                for (String k : patterns.keySet()) {
                    if (c.toString().equalsIgnoreCase(k)) {
                        tokens.add(new Pattern(c.toString()));
                        builder = new StringBuilder();
                    }
                }
            }
            LOGGER.trace(builder.toString());
            //Search for keywords.
            for (String k : keywords.keySet()) {
                if (builder.toString().equalsIgnoreCase(k)) {
                    tokens.add(new Keyword(builder.toString()));
                    builder = new StringBuilder();
                }
            }
            //Search for patterns.
            for (String k : patterns.keySet()) {
                if (builder.toString().equalsIgnoreCase(k)) {
                    tokens.add(new Pattern(builder.toString()));
                    builder = new StringBuilder();
                }
            }
            //Search for types.
            for (String k : types.keySet()) {
                if (builder.toString().equalsIgnoreCase(k)) {
                    tokens.add(new Type(builder.toString()));
                    builder = new StringBuilder();
                }
            }
        }
        if (!tokens.contains(new Pattern(":"))) tokens.add(new End());
        LOGGER.debug(tokens.toString());
        return tokens;
    }

    public void compile(Reader r) throws IOException {
        final List<String> lines = this.preProcess(this.getLines(r));
        compileThread = new Thread(() -> {
            for (String line : lines) {
                queue.add((ArrayList<Token>) this.generateTokenStream(line));
            }
            this.compiled = true;
        });
        compileThread.start();
    }

    public ExpParser run() {
        runtimeThread = new Thread(this::blockedRun);
        runtimeThread.start();
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public ExpParser blockedRun() {
        runMethod(queue);
        return this;
    }

    @Contract()
    @SuppressWarnings({"IdempotentLoopBody", "UnusedReturnValue", "StatementWithEmptyBody"})
    protected Value runMethod(Queue<ArrayList<Token>> tokens) {
        List<Token> code = null;
        AtomicReference<Value> returnValue = new AtomicReference<>(null);
        while (code == null) {
            code = tokens.peek();
        }
        code = tokens.poll();
        while (!compiled) {
            if (code == null) break;
            String string = code.get(0).name;
            List<Token> finalCode = code;
            Runnable method = () -> {
                if (finalCode.get(0) instanceof Name) {
                    if (commands.get(string) != null) {
                        new CommandParser().parset(finalCode);
                    } else if (methods.get(string) != null) {
                        runMethod(methods.get(string).code);
                    }
                } else if (finalCode.get(0) instanceof Keyword) {
                    if ((((Keyword) finalCode.get(0)).keyword) == Keywords.RETURN && finalCode.get(1) instanceof Value) {
                        returnValue.set((Value) finalCode.get(1));
                    }
                }
            };
            if (returnValue.get() != null) return returnValue.get();
            runtimeThreadPool.submit(method);
            runtimeThreadPool.shutdown();
            while (!runtimeThreadPool.isTerminated()) ;
            code = tokens.poll();
        }
        return null;
    }

    public void parse(Reader reader) throws IOException {
        compile(reader);
        LOGGER.debug("Queue: " + queue.toString());
        run();
    }


}
