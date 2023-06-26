package bscript.heap;

import bscript.exception.HeapAccessException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
    public HeapElement[] newHeap;
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

    public boolean put(@NotNull HeapElement element, int index) {
        checkNotNull(element);
        if (useCache) while (useCache) Thread.onSpinWait();
        if (heap[index] != null) return false;
        else synchronized (lock) {
            heap[index] = element;
        }
        return true;
    }

    public void set(@NotNull HeapElement element, int index) {
        checkNotNull(element);
        if (index >= heap.length) return;
        if (!useCache) heap[index] = element;
        if (!(index >= cache.length)) {
            cache[index - count] = element;
        }
        newHeap[index] = element;
    }

    public void mark(int index) {
        checkNotNull(heap[index]);
        heap[index].marked = true;
    }

    public @Nullable HeapElement read(int index) {
        if (index >= heap.length) return null;
        if (!useCache) return heap[index];
        if (!(index >= cache.length)) {
            return cache[index - count];
        }
        HeapElement element = heap[index];
        if (element == null) {
            element = newHeap[index];
        }
        return element;
    }

    public @NotNull HeapElement get(int index) {
        if (index >= heap.length) throw new HeapAccessException("Heap index out of the heap's space");
        if (!useCache) {
            HeapElement element = heap[index];
            if (element == null) throw new HeapAccessException("Accessed a undefined heap space");
            return element;
        }
        if (!(index >= cache.length)) {
            return cache[index - count];
        }
        HeapElement element = heap[index];
        if (element == null) {
            element = newHeap[index];
        }
        if (element == null) throw new HeapAccessException("Accessed a undefined heap space");
        return element;
    }

    public void sweep() {
        cache = null;
    }

    public void clean() {
        synchronized (this) {
            cache = new HeapElement[heap.length / 2];
            synchronized (lock) {
                useCache = true;
            }
            for (int i = 0; i < heap.length; i++) {
                HeapElement element = heap[i];
                if (element == null) continue;
                if (!element.marked) {
                    heap[i] = null;
                    freed.push(i);
                }
                heap[i].marked = false;
            }
            synchronized (lock) {
                useCache = true;
                if (cache.length >= heap.length - count) grow();
                System.arraycopy(cache, 0, heap, count, cache.length);
            }
        }
    }

    public void grow() {
        LOGGER.debug("Heap growing");
        newHeap = new HeapElement[heap.length * grow];
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
                if (cache.length >= heap.length - count) grow();
                System.arraycopy(cache, 0, heap, count, cache.length);
            }
        }
    }

    public void markFreed() {
        blockCache();
        freed.clear();
        for (int i = 0; i < count; i++) {
            if (heap[i] == null) freed.push(i);
        }
    }

    public void blockCache() {
        if (useCache) while (useCache) Thread.onSpinWait();
    }
}
