package net.liyze.basin.http;

import org.jetbrains.annotations.Contract;
import org.smartboot.http.server.HttpRequest;
import org.smartboot.http.server.HttpResponse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public record GetDispatcher(Object instance, Method method, Class<?>[] argTypes) {
    @Contract(pure = true)
    public ModelAndView invoke(HttpRequest request, HttpResponse response) throws InvocationTargetException, IllegalAccessException {
        Object[] args = new Object[argTypes.length];
        for (int i = 0; i < argTypes.length; i++) {
            Class<?> type = argTypes[i];
            if (type == HttpRequest.class) {
                args[i] = request;
            } else if (type == HttpResponse.class) {
                args[i] = response;
            } else if (type == String.class) {
                args[i] = request.getRequestURI();
            } else {
                throw new RuntimeException("Missing handler for type: " + type);
            }
        }
        return (ModelAndView) method.invoke(instance, args);
    }
}
