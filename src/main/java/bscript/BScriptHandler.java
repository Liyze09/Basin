package bscript;

import bscript.exception.ByteCodeLoadingException;
import bscript.nodes.Tree;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static bscript.DefaultBScriptHandler.conf;
import static net.liyze.basin.core.Main.cfg;

public abstract class BScriptHandler {
    static final Logger LOGGER = LoggerFactory.getLogger(BScriptHandler.class);
    @Contract(pure = true)
    public static @NotNull BScriptHandler fromByteCode(byte[] bytes) throws ByteCodeLoadingException {
        var bc = (Bytecode) conf.get().asObject(bytes);
        if (bc.version() > bcv) {
            throw new ByteCodeLoadingException("Bytecode version is too high! Please use higher BScript");
        }
        BScriptHandler bs;
        try {
            bs = (BScriptHandler) Class.forName(cfg.defaultBScriptHandler).getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException e) {
            throw new ByteCodeLoadingException("Unknown BScript handler");
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new ByteCodeLoadingException("Invalid BS handler");
        }
        bs.source = bc.source();
        bs.syntaxTree = bc.syntax();
        bs.byteCodeVersion = bc.version();
        LOGGER.info("Generated a new BS handler with bytecode.");
        return bs;
    }
    @Contract(pure = true)
    public static @NotNull BScriptHandler fromInputStream(@NotNull InputStream stream) throws ByteCodeLoadingException, IOException {
        byte[] bytes = stream.readAllBytes();
        var bc = (Bytecode) conf.get().asObject(bytes);
        if (bc.version() > bcv) {
            throw new ByteCodeLoadingException("Bytecode version is too high! Please use higher BScript");
        }
        BScriptHandler bs;
        try {
            bs = (BScriptHandler) Class.forName(cfg.defaultBScriptHandler).getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException e) {
            throw new ByteCodeLoadingException("Unknown BScript handler");
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new ByteCodeLoadingException("Invalid BS handler");
        }
        bs.source = bc.source();
        bs.syntaxTree = bc.syntax();
        bs.byteCodeVersion = bc.version();
        LOGGER.info("Generated a new BS handler with bytecode.");
        return bs;
    }
    @Contract(pure = true)
    public static @NotNull BScriptHandler fromSource(String source) {
        var bs = new DefaultBScriptHandler();
        bs.source=source;
        LOGGER.info("Generated a new BS handler with source string.");
        return bs;
    }
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Contract(pure = true)
    public static @NotNull BScriptHandler fromReader(@NotNull Reader reader) throws IOException {
        var bs = new DefaultBScriptHandler();
        char[] chars = new char[0];
        reader.read(chars);
        bs.source=new String(chars);
        LOGGER.info("Generated a new BS handler with source reader.");
        reader.close();
        return bs;
    }
    public static final int bcv = 0;
    protected int byteCodeVersion = bcv;
    public String source;
    protected boolean withSource = false;
    public transient List<List<String>> tokenStream;
    public Tree syntaxTree = new Tree();
    @Contract(pure = true)
    public byte @NotNull [] toByteCode() {
        if (!isWithSource()) this.source = "#";
        LOGGER.info("Saving bytecode.");
        return conf.get().asByteArray(new Bytecode(this.byteCodeVersion, this.syntaxTree, this.source));
    }
    protected abstract List<String> preProcess(@NotNull Reader r, Path path);
    protected abstract void generateTokenStream(@NotNull List<String> lines);
    protected abstract void generateSyntaxTree();
    public void compile() {
        generateTokenStream(preProcess(new StringReader(source), Path.of("data/home")));
        generateSyntaxTree();
    }
    protected final transient Map<String, String> stringVars = new ConcurrentHashMap<>();
    protected final transient Map<String, Character> charVars = new ConcurrentHashMap<>();
    protected final transient Map<String, Integer> intVars = new ConcurrentHashMap<>();
    protected final transient Map<String, Long> longVars = new ConcurrentHashMap<>();
    protected final transient Map<String, Short> shortVars = new ConcurrentHashMap<>();
    protected final transient Map<String, Float> floatVars = new ConcurrentHashMap<>();
    protected final transient Map<String, Double> doubleVars = new ConcurrentHashMap<>();
    protected final transient Map<String, Boolean> boolVars = new ConcurrentHashMap<>();
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
