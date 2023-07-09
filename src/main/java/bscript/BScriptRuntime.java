package bscript;

import bscript.exception.BroadcastException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class BScriptRuntime implements Runnable, Callable<Integer> {
    private final Logger LOGGER = LoggerFactory.getLogger(BScriptRuntime.class);
    private final Class<?> clazz;
    private final OutputBytecode instance;
    private final Map<String, Method> handlers = new HashMap<>();

    public BScriptRuntime(Class<?> clazz, OutputBytecode instance) {
        this.clazz = clazz;
        this.instance = instance;
    }

    @Override
    public void run() {
        call();
    }

    @Contract(pure = true)
    public @Nullable Class<?> getClazz() {
        return this.clazz;
    }

    @Contract
    @Override
    public @NotNull Integer call() {
        try (final ExecutorService pool = Executors.newCachedThreadPool()) {
            final ArrayList<Method> methods = new ArrayList<>(List.of(clazz.getDeclaredMethods()));
            instance.init();
            methods.forEach(method -> handlers.put(method.getName(), method));
            methods.clear();
            broadcast("main");
        }
        return 0;
    }

    public void broadcast(String event) {
        Method method = handlers.get(event);
        if (method != null && method.trySetAccessible()) try {
            method.invoke(instance);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new BroadcastException();
        }
    }
}
