package bscript;

import bscript.exception.ByteCodeLoadingException;
import bscript.heap.HeapManager;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import org.jetbrains.annotations.NotNull;

import static bscript.BScriptCompiler.KRYO_POOL;

public class BScriptRuntime {
    public Tree syntaxTree = new Tree();
    public HeapManager heap = new HeapManager(512);

    private BScriptRuntime() {

    }

    public static @NotNull BScriptRuntime fromBytecodeObject(@NotNull Bytecode bytecode) {
        BScriptRuntime runtime = new BScriptRuntime();
        runtime.syntaxTree = bytecode.syntax();
        return runtime;
    }

    public static @NotNull BScriptRuntime fromBytecode(byte[] bytes) {
        Kryo k = KRYO_POOL.obtain();
        try (Input input = new Input()) {
            Bytecode bytecode = k.readObject(input, Bytecode.class);
            return fromBytecodeObject(bytecode);
        } catch (Exception e) {
            throw new ByteCodeLoadingException(e.getMessage());
        } finally {
            KRYO_POOL.free(k);
        }
    }

}
