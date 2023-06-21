package bscript;

import bscript.nodes.Element;
import bscript.nodes.Tree;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.Pool;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public abstract class BScriptHandler {
    public static final int bcv = 0;
    static final Logger LOGGER = LoggerFactory.getLogger(BScriptHandler.class);
    public String source;
    public transient List<List<String>> tokenStream;
    public Tree syntaxTree = new Tree();
    protected int byteCodeVersion = bcv;
    protected boolean withSource = false;
    public final Pool<Kryo> kryo = new Pool<>(true, true) {
        protected @NotNull Kryo create() {
            Kryo kryo = new Kryo();
            kryo.register(Bytecode.class, 1);
            return kryo;
        }
    };

    // Factories
    @Contract(pure = true)
    public static @NotNull BScriptHandler fromSource(String source) {
        var bs = new DefaultBScriptHandler();
        bs.source = source;
        LOGGER.info("Generated a new BS handler with source string.");
        return bs;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Contract(pure = true)
    public static @NotNull BScriptHandler fromReader(@NotNull Reader reader) throws IOException {
        var bs = new DefaultBScriptHandler();
        char[] chars = new char[0];
        reader.read(chars);
        bs.source = new String(chars);
        LOGGER.info("Generated a new BS handler with source reader.");
        reader.close();
        return bs;
    }

    public Bytecode toBytecodeObject() {
        if (!withSource) {
            this.source = "#\nignored";
        }
        return new Bytecode(this.byteCodeVersion, this.syntaxTree, this.source);
    }

    public byte[] toBytecode() {
        try (Output output = new Output()) {
            Kryo k = kryo.obtain();
            k.writeObject(output, this.toBytecodeObject());
            kryo.free(k);
            return output.toBytes();
        }
    }

    protected abstract List<String> preProcess(@NotNull Reader r, Path path);

    protected abstract void generateTokenStream(@NotNull List<String> lines);

    protected abstract void generateSyntaxTree();

    public void compile() {
        generateTokenStream(preProcess(new StringReader(source), Path.of("data/home")));
        generateSyntaxTree();
    }

    //Developer-Utils------------------------------------------------------------------------------------
    public final void printTokenStream() {
        System.out.println(this + " TokenStream:\n" + tokenStream.toString());
    }

    public final void printSyntaxTree() {
        System.out.println(this + " SyntaxTree:\n");
        for (Map.Entry<Integer, Element> i : this.syntaxTree.tree.entrySet()) {
            System.out.print(i.getKey() + " ");
            System.out.println(i.getValue().toString());
        }
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
