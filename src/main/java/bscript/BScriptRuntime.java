package bscript;

import bscript.exception.ByteCodeLoadingException;
import bscript.heap.HeapManager;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static bscript.BScriptCompiler.*;

public abstract class BScriptRuntime {
    public Tree syntaxTree = new Tree();
    public List<String> args = new ArrayList<>();
    public HeapManager heap = new HeapManager(512);

    protected BScriptRuntime() {

    }

    public static @NotNull BScriptRuntime fromBytecodeObject(@NotNull Bytecode bytecode) {
        BScriptRuntime runtime = new DefaultBScriptRuntime();
        runtime.syntaxTree = bytecode.syntax();
        runtime.args = bytecode.args();
        LOGGER.info("Start a new BScript VM");
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

    public abstract void invoke();
}
