package bscript;

import bscript.mem.MemoryManager;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import org.jetbrains.annotations.NotNull;

import static bscript.BScriptCompiler.KRYO_POOL;

public class BScriptRuntime {
    public Tree syntaxTree = new Tree();
    public MemoryManager heap = new MemoryManager(512);

    private BScriptRuntime() {

    }

    public static @NotNull BScriptRuntime fromBytecodeObject(@NotNull Bytecode bytecode) {
        BScriptRuntime runtime = new BScriptRuntime();
        runtime.syntaxTree = bytecode.syntax();
        return runtime;
    }

    public static @NotNull BScriptRuntime fromBytecode(byte[] bytes) {
        try (Input input = new Input()) {
            Kryo k = KRYO_POOL.obtain();
            Bytecode bytecode = k.readObject(input, Bytecode.class);
            KRYO_POOL.free(k);
            return fromBytecodeObject(bytecode);
        }
    }
}
