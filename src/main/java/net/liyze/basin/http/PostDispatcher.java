package net.liyze.basin.http;

import com.google.gson.Gson;
import org.jetbrains.annotations.Contract;
import org.smartboot.http.server.HttpRequest;
import org.smartboot.http.server.HttpResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public record PostDispatcher(Object instance, Method method, Class<?>[] argTypes, Gson gson) {
    @Contract(pure = true)
    public ModelAndView invoke(HttpRequest request, HttpResponse response) throws InvocationTargetException, IllegalAccessException, IOException {
        Object[] args = new Object[argTypes.length];
        for (int i = 0; i < argTypes.length; i++) {
            Class<?> type = argTypes[i];
            if (type == HttpRequest.class) {
                args[i] = request;
            } else if (type == HttpResponse.class) {
                args[i] = response;
            } else {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] bytes = new byte[1024];
                int len;
                InputStream inputStream = request.getInputStream();
                while ((len = inputStream.read(bytes)) != -1) {
                    out.write(bytes, 0, len);
                }
                args[i] = gson.fromJson(out.toString(request.getCharacterEncoding()), type);
            }
        }
        return (ModelAndView) method.invoke(instance, args);
    }
}
