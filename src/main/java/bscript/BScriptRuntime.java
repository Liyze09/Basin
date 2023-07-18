package bscript;

import bscript.exception.BScriptException;
import bscript.exception.BroadcastException;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class BScriptRuntime implements Runnable, AutoCloseable {

    public final ExecutorService pool = Executors.newCachedThreadPool();
    private final MultiMap<String, Method> handlers = new MultiMap<>();

    public BScriptRuntime() {

    }

    @Override
    public void run() {
        broadcast("main");
    }

    public void broadcast(@NotNull String event) {
        broadcast(event, null);
    }

    public void broadcast(String event, BScriptEvent body) {
        pool.submit(() -> directBroadcast(event, body));
    }

    public void directBroadcast(@NotNull String event, BScriptEvent body) {
        if (event.equals("exception") && handlers.get("exceptionEvent").size() == 0)
            throw new BScriptException((Throwable) body.body()[0]);
        handlers.get(event + "Event").forEach(method -> {
            boolean b = !event.equals("before") && !event.equals("around") && !event.equals("broadcast") && !event.equals("after") && !event.equals("exception");
            if (b) broadcast("broadcast");
            if (method.trySetAccessible()) try {
                if (b) directBroadcast("before", null);
                if (b) directBroadcast("around", null);
                method.invoke(null, new BScriptEvent(event, body));
                if (b) directBroadcast("around", null);
                if (b) directBroadcast("after", null);
            } catch (IllegalAccessException e) {
                throw new BroadcastException();
            } catch (InvocationTargetException e) {
                broadcast("exception", new BScriptEvent("exception", e.getCause()));
            }
        });
    }

    public void load(@NotNull Class<?> clazz) {
        Arrays.stream(clazz.getDeclaredMethods()).filter(method -> method.getName().endsWith("Event"))
                .forEach(method -> this.handlers.put(method.getName(), method));
        broadcast("loadBean");
    }

    public void close() {
        broadcast("shutdown");
        pool.shutdown();
        try {
            pool.awaitTermination(500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        handlers.clear();
    }

    public static class MultiMap<K, V> {
        private final Map<K, Set<V>> map = new ConcurrentHashMap<>();

        public void put(K key, V value) {
            if (!map.containsKey(key)) {
                map.put(key, new HashSet<>());
            }
            map.get(key).add(value);
        }

        public Collection<V> get(K key) {
            if (!map.containsKey(key)) return new ArrayList<>();
            return new ArrayList<>(map.get(key));
        }

        public void clear() {
            map.clear();
        }
    }
}
