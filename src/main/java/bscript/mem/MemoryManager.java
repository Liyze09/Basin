package bscript.mem;

import java.util.ArrayDeque;
import java.util.Deque;

import static com.google.common.base.Preconditions.checkNotNull;

public final class MemoryManager {
    public MemoryElement[] heap;
    public Deque<Integer> freed = new ArrayDeque<>();
    public int count = 0;

    public MemoryManager(int size) {
        heap = new MemoryElement[size];
    }

    public int put(MemoryElement element) {
        checkNotNull(element);
        int index;
        if (freed.size() != 0) {
            index = freed.pop();
        } else synchronized (this) {
            index = count++;
        }
        synchronized (this) {
            heap[index] = element;
        }
        return index;
    }

    public boolean put(MemoryElement element, int index) {
        checkNotNull(element);
        if (heap[index] == null) return false;
        else synchronized (this) {
            heap[index] = element;
        }
        return true;
    }

    public void set(MemoryElement element, int index) {
        heap[index] = element;
    }

    public void mark(int index) {
        checkNotNull(heap[index]);
        heap[index].marked = true;
    }

    public void clean() {
        for (int i = 0; i < heap.length; i++) {
            MemoryElement element = heap[i];
            if (!element.marked) {
                heap[i] = null;
            }
        }
    }

    public void grow() {
        MemoryElement[] newHeap = new MemoryElement[heap.length * 2];
        synchronized (this) {
            for (int i = 0; i < heap.length; i++) {
                MemoryElement element = heap[i];
                if (!element.marked) {
                    newHeap[i] = null;
                }
            }
            this.heap = newHeap;
        }
    }
}
