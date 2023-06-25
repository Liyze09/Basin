package bscript.mem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;

import static com.google.common.base.Preconditions.checkNotNull;

public final class HeapManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(HeapManager.class);
    private final Object lock = new Object();
    private final Object lock0 = new Object();
    public HeapElement[] heap;
    public volatile boolean useCache = false;
    public HeapElement[] cache;
    public Deque<Integer> freed = new ArrayDeque<>();
    public int cacheCount = 0;
    public int count = 0;
    public byte grow = 2;

    public HeapManager(int size) {
        heap = new HeapElement[size];
    }

    public int put(HeapElement element) {
        checkNotNull(element);
        int index;
        if (useCache) {
            synchronized (lock0) {
                index = cacheCount++;
                if (index > cache.length) {
                    while (useCache) Thread.onSpinWait();
                    return put(element);
                }
            }
            synchronized (lock) {
                cache[index] = element;
            }
            return index;
        }
        if (freed.size() != 0) {
            index = freed.pop();
        } else synchronized (lock0) {
            index = count++;
        }
        if (index >= heap.length) {
            grow();
            return put(element);
        }
        synchronized (lock) {
            heap[index] = element;
        }
        return index;
    }

    public boolean put(HeapElement element, int index) {
        checkNotNull(element);
        if (useCache) while (useCache) Thread.onSpinWait();
        if (heap[index] != null) return false;
        else synchronized (lock) {
            heap[index] = element;
        }
        return true;
    }

    public void set(HeapElement element, int index) {
        if (useCache) while (useCache) Thread.onSpinWait();
        heap[index] = element;
    }

    public void mark(int index) {
        checkNotNull(heap[index]);
        heap[index].marked = true;
    }

    public void clean() {
        synchronized (this) {
            cache = new HeapElement[heap.length / 2];
            synchronized (lock) {
                useCache = true;
            }
            for (int i = 0; i < heap.length; i++) {
                HeapElement element = heap[i];
                if (!element.marked) {
                    heap[i] = null;
                    freed.push(i);
                }
            }
            synchronized (lock) {
                useCache = true;
                System.arraycopy(cache, 0, heap, count, cache.length);
            }
        }
        cache = null;
    }

    public void grow() {
        LOGGER.debug("Heap growing");
        HeapElement[] newHeap = new HeapElement[heap.length * grow];
        synchronized (this) {
            cache = new HeapElement[0];
            synchronized (lock) {
                useCache = true;
            }
            for (int i = 0; i < heap.length; i++) {
                newHeap[i] = heap[i];
                heap[i] = null;
            }
            this.heap = newHeap;
            synchronized (lock) {
                useCache = false;
                System.arraycopy(cache, 0, heap, count, cache.length);
            }
        }
        cache = null;
    }
}
