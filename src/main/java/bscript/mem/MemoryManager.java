package bscript.mem;

import java.util.ArrayDeque;
import java.util.Deque;

public class MemoryManager {
    public MemoryElement[] mem;
    public Deque<Integer> freed = new ArrayDeque<>();

    public MemoryManager(int size) {
        mem = new MemoryElement[size];
    }

    public int put(MemoryElement element) {
        if (freed.size() != 0) {
            mem[freed.pop()] = element;
        }
        for (int i = 0; i < mem.length; ++i) {
            if (mem[i] == null) {
                mem[i] = element;
                return i;
            }
        }
        return -1;
    }
}
