package net.liyze.basin.rpc;

import com.itranswarp.summer.BeanDefinition;
import io.fury.Fury;
import io.fury.Language;
import io.fury.ThreadSafeFury;
import net.liyze.basin.core.Server;
import net.liyze.basin.rpc.annotation.RpcService;
import net.liyze.basin.util.ContextUtils;
import org.jetbrains.annotations.NotNull;
import org.smartboot.socket.extension.protocol.ByteArrayProtocol;
import org.smartboot.socket.transport.AioQuickServer;
import org.smartboot.socket.transport.WriteBuffer;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RpcServer implements Server {
    private static final ThreadSafeFury FURY = Fury.builder()
            .withLanguage(Language.JAVA)
            .withRefTracking(true)
            .requireClassRegistration(false)
            .buildThreadSafeFuryPool(1, 20, 3, TimeUnit.SECONDS);
    public final int port;
    private final AioQuickServer server;

    public RpcServer(int port) {
        server = new AioQuickServer(port, new ByteArrayProtocol(), ((session, msg) -> {
            final String className;
            final String methodName;
            final List<Object> args = new ArrayList<>();
            final WriteBuffer stream = session.writeBuffer();
            final int sign;
            try (var bytes = new DataInputStream(new ByteArrayInputStream(msg))) {
                sign = bytes.readInt();
                className = new String(bytes.readNBytes(bytes.readInt()), StandardCharsets.UTF_8);
                methodName = new String(bytes.readNBytes(bytes.readInt()), StandardCharsets.UTF_8);
                int argCount = bytes.readInt();
                for (int i = 0; i <= argCount; ++i) {
                    args.add(FURY.deserialize(bytes.readNBytes(bytes.readInt())));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            BeanDefinition bean = ContextUtils.getBean(className);
            if (bean != null) {
                try {
                    Object ret = invoke(bean.getBeanClass(), methodName, args.toArray(), bean.getInstance());
                    stream.writeInt(sign);
                    stream.writeByte((byte) 0);
                    stream.writeAndFlush(FURY.serialize(ret));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                stream.writeByte((byte) 1);
            }
        }));
        this.port = port;
    }

    @Override
    public void stop() {
        server.shutdown();
    }

    @Override
    public Server start() {
        server.setLowMemory(true);
        try {
            server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    private Object invoke(@NotNull Class<?> clazz, String name, Object[] args, Object object)
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
}
