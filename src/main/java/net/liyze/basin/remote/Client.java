package net.liyze.basin.remote;

import bscript.BScriptEvent;
import org.jetbrains.annotations.NotNull;
import org.smartboot.socket.MessageProcessor;
import org.smartboot.socket.extension.protocol.StringProtocol;
import org.smartboot.socket.transport.AioQuickClient;
import org.smartboot.socket.transport.AioSession;
import org.smartboot.socket.transport.WriteBuffer;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static net.liyze.basin.core.Main.*;

public class Client {
    /**
     * Send {@code message} to {@code host}:{@code port} use {@code token}.
     *
     * @param message Message
     * @param host    Remote host
     * @param token   Remote basin token
     * @param port    Remote port
     */
    public static void send(String message, String host, @NotNull String token, int port) throws Exception {
        runtime.broadcast("remoteClientSending", new BScriptEvent("remoteClientSending", message, host, port));
        MessageProcessor<String> processor = (session, msg) -> LOGGER.info("Receive from server: " + msg);
        AioQuickClient client = new AioQuickClient(host, port, new StringProtocol(), processor);
        AioSession session = client.start();
        WriteBuffer writeBuffer = session.writeBuffer();
        byte[] msg = ("brc:" + message).getBytes(StandardCharsets.UTF_8);

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKey keySpec = new SecretKeySpec(token.getBytes(StandardCharsets.UTF_8), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        msg = cipher.doFinal(msg);

        writeBuffer.writeInt(msg.length);
        writeBuffer.write(msg);
        writeBuffer.flush();
        runtime.broadcast("remoteClientSent", new BScriptEvent("remoteClientSent", message, host, port));
        LOGGER.info("Remote Sent: \"{}\" to \"{}\":{}", message, host, port);
    }

    /**
     * Send {@code <message>} to {@code <host>}:default port use {@code <token>}.
     */
    public static void send(String message, String host, @NotNull String token) throws Exception {
        send(message, host, token, cfg.remotePort);
    }
}
