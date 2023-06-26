package bscript.test;

import bscript.DefaultBScriptCompiler;
import bscript.heap.HeapElement;
import bscript.heap.HeapManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static java.lang.System.out;

@DisplayName("BScript Test 1")
public final class ScriptTest {
    @Test
    public void compileTest() {
        var bs = DefaultBScriptCompiler.fromSource(
                """
                        print("start")
                        var i=0
                        loop:
                        \ti=i+1
                        \tprint(i)
                        \tif(i>10):
                        \t\tprint("stopping")
                        \t\tbreak
                        print("finished")
                        """);
        bs.compile();
        bs.printTokenStream();
        bs.printSyntaxTree();
    }

    @Test
    public void heapTest() {
        HeapManager heap = new HeapManager(8);
        for (int i = 0; i < 8; i++) {
            heap.put(new HeapElement());
        }
        for (int i = 0; i < 32; i++) {
            heap.put(new HeapElement());
        }
        out.println(heap.read(1));
        out.println(Arrays.toString(heap.heap));
    }
}
