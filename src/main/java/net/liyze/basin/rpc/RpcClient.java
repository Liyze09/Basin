package net.liyze.basin.rpc;

import io.fury.Fury;
import io.fury.Language;
import io.fury.ThreadSafeFury;
import org.jetbrains.annotations.NotNull;
import org.smartboot.socket.extension.protocol.ByteArrayProtocol;
import org.smartboot.socket.transport.AioQuickClient;
import org.smartboot.socket.transport.AioSession;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public final class RpcClient {
    private static final Map<ServiceID, RpcClient> clientMap = new ConcurrentHashMap<>();

    public static @NotNull RpcClient getRpcClient(String host, int port) {
        var id = new ServiceID(host, port);
        var client = clientMap.get(id);
        if (client != null) return client;
        try {
            client = new RpcClient(host, port);
            clientMap.put(id, client);
            return client;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public record ServiceID(String host, int port) {
    }

    private static final ThreadSafeFury FURY = Fury.builder()
            .withLanguage(Language.JAVA)
            .withRefTracking(true)
            .requireClassRegistration(false)
            .buildThreadSafeFuryPool(1, 7, 7, TimeUnit.SECONDS);
    private static final Random random = new Random();
    public final ServiceID ID;
    private final AioQuickClient client;
    private final AioSession session;
    private final Map<Integer, Optional<Object>> map = new ConcurrentHashMap<>();

    RpcClient(String host, int port) throws IOException {
        ID = new ServiceID(host, port);
        this.client = new AioQuickClient(host, port, new ByteArrayProtocol(), ((session, msg) -> {
            try (var bytes = new DataInputStream(new ByteArrayInputStream(msg))) {
                int sign = bytes.readInt();
                byte status = bytes.readByte();
                Optional<Object> ret;
                if (status != 0) {
                    ret = Optional.empty();
                } else {
                    ret = Optional.of(FURY.deserialize(bytes.readAllBytes()));
                }
                map.put(sign, ret);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));
        session = client.start();
    }

    public void shutdown() {
        client.shutdown();
    }

    public int request(@NotNull String className, @NotNull String methodName, Object @NotNull [] args)
            throws IOException {
        if (session.isInvalid()) {
            clientMap.remove(ID, this);
            throw new RuntimeException("Session is closed");
        }
        int sign = random.nextInt();
        byte[] cb = className.getBytes(StandardCharsets.UTF_8);
        byte[] mb = methodName.getBytes(StandardCharsets.UTF_8);
        var out = session.writeBuffer();
        out.writeInt(sign);

        out.writeInt(cb.length);
        out.write(cb);

        out.writeInt(mb.length);
        out.write(mb);
        out.writeInt(args.length);
        for (Object arg : args) {
            byte[] bytes = FURY.serialize(arg);
            out.writeInt(bytes.length);
            out.write(bytes);
        }
        out.flush();
        return sign;
    }

    public Object get(int sign, long timeout) {
        long time = System.currentTimeMillis() + timeout;
        while (!map.containsKey(sign) && time > System.currentTimeMillis()) Thread.onSpinWait();
        return map.get(sign);
    }
}
