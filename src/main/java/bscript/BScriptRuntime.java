package bscript;

import bscript.exception.BScriptException;
import bscript.exception.BroadcastException;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class BScriptRuntime implements Runnable, AutoCloseable {
    private final Logger LOGGER = LoggerFactory.getLogger(BScriptRuntime.class);
    public final ExecutorService pool = Executors.newCachedThreadPool();
    private final Multimap<String, Method> handlers = HashMultimap.create();

    public BScriptRuntime() {

    }

    @Override
    public void run() {
        LOGGER.debug("{} started.", this);
        broadcast("main");
    }

    public void broadcast(@NotNull String event) {
        broadcast(event, null);
    }

    public void broadcast(String event, BScriptEvent body) {
        pool.submit(() -> directBroadcast(event, body));
    }

    private void directBroadcast(String event, BScriptEvent body) {
        if (event.equals("exception") && handlers.get("exceptionEvent").size() == 0)
            throw new BScriptException((Throwable) body.body()[0]);
        handlers.get(event + "Event").forEach(method -> {
            LOGGER.debug("Event: {}", event);
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
        LOGGER.debug("Loading a new bean.");
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
        LOGGER.debug("{} closed.", this);
    }
}
