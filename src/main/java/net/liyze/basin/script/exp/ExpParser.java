package net.liyze.basin.script.exp;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

import static net.liyze.basin.script.exp.Keywords.*;
import static net.liyze.basin.script.exp.Patterns.*;

@SuppressWarnings("unused")
public class ExpParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExpParser.class);
    public String name;
    protected Queue<Task> queue = new ConcurrentLinkedQueue<>();
    public static Map<String, Keywords> keywords;
    public static Map<String, Patterns> patterns;
    public Map<String, Function<List<String>, List<String>>> annotations = new HashMap<>();
    protected boolean enableCheck = true;
    protected Map<String, Integer> intVars = new HashMap<>();
    protected Map<String, Double> floatVars = new HashMap<>();
    protected Map<String, String> stringVars = new HashMap<>();
    protected Map<String, Boolean> booleanVars = new HashMap<>();

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

    public boolean serialize() {
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream("data" + File.separator + "out" + File.separator + this.name + ".bsc"));
            outputStream.writeObject(queue);
            outputStream.flush();
            outputStream.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public static @NotNull ExpParser loadFrom(InputStream s) throws IOException, ClassNotFoundException {
        ExpParser parser = new ExpParser();
        ObjectInputStream stream = new ObjectInputStream(s);
        parser.queue = (Queue<Task>) stream.readObject();
        return parser;
    }

    public List<String> getLines(Reader r) throws IOException {
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
                    if (line.startsWith("@NonCheck")) {
                        this.enableCheck = false;
                    } else if (line.startsWith("@Include")) {
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
        // def func(string str):  ->  [k.DEF, "func", p.LK, k.STRING, "str", p.RK, p.MH]
        for (Character c : code.toCharArray()) {
            if (words.contains(c) && !builder.toString().isBlank()) {
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
        if (tokens.contains(new Pattern(":"))) tokens.add(new End());
        return tokens;
    }

    protected void checkTokens(@NotNull List<Token> tokens) {
        int in1 = 0;
        int in2 = 0;
        int in3 = 0;
        Token token;
        for (int i = 0; i < tokens.size(); ++i) {
            token = tokens.get(i);
            if (token instanceof Pattern) {
                if (((Pattern) token).pattern == LK) {
                    in1++;
                } else if (((Pattern) token).pattern == RK) {
                    in1--;
                } else if (((Pattern) token).pattern == LD) {
                    in2++;
                } else if (((Pattern) token).pattern == RD) {
                    in2--;
                } else if (((Pattern) token).pattern == LZ) {
                    in3++;
                } else if (((Pattern) token).pattern == RZ) {
                    in3--;
                }
            } else if (token instanceof Keyword) {
                if (((Pattern) tokens.get(i+2)).pattern==FZ) {
                    if (((Keyword) token).keyword == STRING) {
                        String value = tokens.get(i + 3).name;
                        if (value.startsWith("\"") && value.endsWith("\"")) {
                            value = value.substring(1, value.length() - 1);
                            //
                        } else LOGGER.error("Invalid String: {} on {}", value, i+1);
                    } else if (((Keyword) token).keyword == INT) {
                        Token sign = tokens.get(i+1);
                        String value = tokens.get(i+1).name;
                        int integer;
                        if (value.matches("\\w*")) {
                            integer = Integer.parseInt(value);
                            //
                        } else LOGGER.error("Invalid Integer: {} on {}", value, i+1);
                    } else if (((Keyword) token).keyword == FLOAT) {
                        Token sign = tokens.get(i+1);
                        String value = tokens.get(i+1).name;
                        double num;
                        if (value.matches("\\w*")) {
                            num = Double.parseDouble(value);
                            //
                        } else LOGGER.error("Invalid Float: {} on {}", value, i+1);
                    } else if (((Keyword) token).keyword == BOOLEAN) {
                        Token sign = tokens.get(i+1);
                        String value = tokens.get(i+1).name;
                        boolean bool;
                        if (value.matches("true|false")) {
                            bool = Boolean.parseBoolean(value);
                            //
                        } else LOGGER.error("Invalid Boolean: {} on {}", value, i+1);
                    }
                }
            } else if (token instanceof Name) {

            }
        }
    }
}
