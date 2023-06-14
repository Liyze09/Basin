package net.liyze.basin.script.exp;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

import static net.liyze.basin.script.exp.Patterns.words;

@SuppressWarnings("unused")
public class ExpParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExpParser.class);
    public String name;
    protected Queue<ArrayList<Token>> queue = new ConcurrentLinkedQueue<>();
    public static Map<String, Keywords> keywords;
    public static Map<String, Patterns> patterns;
    public Map<String, Function<List<String>, List<String>>> annotations = new HashMap<>();
    protected Thread compileThread;
    protected Thread runtimeThread;

    static {
        Map<String, Keywords> rt1 = new HashMap<>();
        Arrays.stream(Keywords.values()).forEach(key -> rt1.put(key.name(), key));
        keywords = Collections.unmodifiableMap(rt1);
        Map<String, Patterns> rt2 = new HashMap<>();
        Arrays.stream(Patterns.values()).forEach(key -> rt2.put(key.name(), key));
        patterns = Collections.unmodifiableMap(rt2);
    }

    public ExpParser() {
    }

    public byte @Nullable [] serialize() {
        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
            objectStream.writeObject(queue);
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

    @SuppressWarnings("unchecked")
    public static @NotNull ExpParser loadFrom(InputStream s) throws IOException, ClassNotFoundException {
        ExpParser parser = new ExpParser();
        ObjectInputStream stream = new ObjectInputStream(s);
        parser.queue = (Queue<ArrayList<Token>>) stream.readObject();
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
                        lines.addAll(i, this.getLines(new FileReader(
                                "data" + File.separator + "home" + line.substring(9).strip(),
                                StandardCharsets.UTF_8)));
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
        // def func(string str):  ->  [k.DEF, "func", p.LK, k.STRING, "str", p.RK, p.MH]
        for (Character c : code.toCharArray()) {
            if (words.contains(c) && !builder.toString().isBlank() && !inString) {
                tokens.add(new Name(builder.toString()));
                builder = new StringBuilder();
            }
            if (c == '\"') {
                tokens.add(new Name(builder.toString()));
                builder = new StringBuilder();
                inString = !inString;
            } else if (c.equals(' ')) {
                tokens.add(new Name(builder.toString()));
                builder = new StringBuilder();
            }
            builder.append(c);
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
        }
        if (!tokens.contains(new Pattern(":"))) tokens.add(new End());
        return tokens;
    }

    public ExpParser compile(Reader r) throws IOException {
        final List<String> lines = this.preProcess(this.getLines(r));
        compileThread = new Thread(() -> {
            for (String line : lines) {
                queue.add((ArrayList<Token>) this.generateTokenStream(line));
            }
        });
        compileThread.start();
        return this;
    }

    public ExpParser run() {
        runtimeThread = new Thread(() -> {
            List<Token> code;
            do {
                code = queue.poll();
            } while (code != null);
        });
        runtimeThread.start();
        return this;
    }
}
