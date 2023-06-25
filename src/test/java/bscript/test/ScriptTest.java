package bscript.test;

import bscript.DefaultBScriptCompiler;
import bscript.mem.HeapElement;
import bscript.mem.HeapManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

@DisplayName("BScript Test 1")
public final class ScriptTest{
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
        HeapManager heap = new HeapManager(4);
        for (int i = 0; i < 4; i++) {
            heap.put(new HeapElement());
        }
        for (int i = 0; i < 8; i++) {
            heap.put(new HeapElement());
        }
        System.out.println(Arrays.toString(heap.heap));
    }
}
