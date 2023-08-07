package net.liyze.basin.rpc;

import com.itranswarp.summer.BeanDefinition;
import io.fury.Fury;
import io.fury.Language;
import io.fury.ThreadSafeFury;
import net.liyze.basin.core.Server;
import net.liyze.basin.rpc.annotation.RpcService;
import net.liyze.basin.util.ContextUtils;
import net.liyze.basin.util.LoggingUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.server.HttpBootstrap;
import org.smartboot.http.server.HttpRequest;
import org.smartboot.http.server.HttpResponse;
import org.smartboot.http.server.HttpServerHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

public final class RpcServer implements Server {
    public final int port;
    public RpcServer(int port) {
        this.port = port;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServer.class);
    private final HttpBootstrap bootstrap = new HttpBootstrap();

    private static final ThreadSafeFury FURY = Fury.builder()
            .withLanguage(Language.JAVA)
            .withRefTracking(true)
            .withAsyncCompilation(true)
            .buildThreadSafeFuryPool(2, 32, 8, TimeUnit.SECONDS);

    private static Object invoke(@NotNull Class<?> clazz, String name, Object[] args, Object object)
            throws InvocationTargetException, IllegalAccessException {
        for (Method method : clazz.getMethods()) {
            if (method.getParameterCount() == args.length
                    && method.getName().equals(name)
                    && method.isAnnotationPresent(RpcService.class)
            ) {
                return method.invoke(object, args);
            }
        }
        throw new IllegalArgumentException();
    }

    @Override
    public void stop() {
        bootstrap.shutdown();
    }

    @Override
    public Server start() {
        bootstrap.configuration().serverName("rpc");
        bootstrap.httpHandler(new HttpServerHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response) throws IOException {
                final String className = request.getHeader("Class-Name");
                final String methodName = request.getHeader("Method-Name");
                final OutputStream stream = response.getOutputStream();
                final Object[] args = (Object[]) FURY.deserialize(request.getInputStream().readAllBytes());
                BeanDefinition bean = ContextUtils.getBean(className);
                response.addHeader("Request-Sign", request.getHeader("Request-Sign"));
                if (bean != null) {
                    try {
                        Object ret = invoke(bean.getBeanClass(), methodName, args, bean.getInstance());
                        FURY.getCurrentFury().register(ret.getClass());
                        stream.write(FURY.serialize(ret));
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        LoggingUtils.printException(LOGGER, e);
                        response.setHttpStatus(HttpStatus.BAD_REQUEST);
                    } catch (InvocationTargetException e) {
                        LoggingUtils.printException(LOGGER, e);
                        response.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                } else {
                    response.setHttpStatus(HttpStatus.BAD_REQUEST);
                }
                stream.flush();
            }
        });
        bootstrap.setPort(port).start();
        return this;
    }
}
