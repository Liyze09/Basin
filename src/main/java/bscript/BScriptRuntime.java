package bscript;

import bscript.exception.BroadcastException;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class BScriptRuntime implements Runnable, AutoCloseable {
    private final Logger LOGGER = LoggerFactory.getLogger(BScriptRuntime.class);
    public final ExecutorService pool = Executors.newCachedThreadPool();
    private final Class<?> clazz;
    private final OutputBytecode instance;
    private final Multimap<String, Method> handlers = HashMultimap.create();

    public BScriptRuntime(Class<?> clazz, OutputBytecode instance) {
        this.clazz = clazz;
        this.instance = instance;
    }

    @Override
    public void run() {
        LOGGER.debug("{} started.", this);
        final ArrayList<Method> methods = new ArrayList<>(List.of(clazz.getDeclaredMethods()));
        instance.init();
        methods.forEach(method -> {
            String name = method.getName();
            if (name.substring(name.length() - 1).matches("\\d")) {
                name = name.substring(0, name.length() - 1);
            }
            handlers.put(name, method);
        });
        methods.clear();
        broadcast("main");
    }

    @Contract(pure = true)
    public @Nullable Class<?> getClazz() {
        return this.clazz;
    }

    public void broadcast(@NotNull String event) {
        LOGGER.debug("Event: {}", event);
        boolean b = !event.equals("before") && !event.equals("around") && !event.equals("broadcast") && !event.equals("after");
        if (b) {
            broadcast("broadcast");
        }
        pool.submit(() -> handlers.get(event + "Event").forEach(method -> {
            if (method.trySetAccessible()) try {
                if (b) broadcast("before");
                if (b) broadcast("around");
                method.invoke(instance);
                if (b) broadcast("around");
                if (b) broadcast("after");
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new BroadcastException();
            }
        }));
    }

    public void load(@NotNull Class<?> clazz, @NotNull OutputBytecode instance) {
        LOGGER.debug("Loading a new bean.");
        final ArrayList<Method> methods = new ArrayList<>(List.of(clazz.getDeclaredMethods()));
        instance.init();
        methods.forEach(method -> {
            String name = method.getName();
            if (name.substring(name.length() - 1).matches("\\w")) {
                name = name.substring(0, name.length() - 1);
            }
            handlers.put(name, method);
        });
        methods.clear();
        broadcast("loadBean");
    }

    public void close() {
        broadcast("shutdown");
        pool.shutdown();
        handlers.clear();
        LOGGER.debug("{} closed.", this);
    }
}
