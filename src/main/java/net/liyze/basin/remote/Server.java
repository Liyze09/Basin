package net.liyze.basin.remote;

import net.liyze.basin.core.Conversation;
import org.jetbrains.annotations.NotNull;
import org.smartboot.socket.MessageProcessor;
import org.smartboot.socket.extension.protocol.ByteArrayProtocol;
import org.smartboot.socket.transport.AioQuickServer;
import org.smartboot.socket.transport.WriteBuffer;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static net.liyze.basin.core.Main.LOGGER;

public class Server {
    public void server(@NotNull String token, int port) throws Exception {
        final Conversation conversation = new Conversation();
        MessageProcessor<byte[]> processor = (s, b) -> {
            Cipher cipher;
            String msg = "";
            try (WriteBuffer outputStream = s.writeBuffer()) {
                try {
                    cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                    SecretKey keySpec = new SecretKeySpec(token.getBytes(StandardCharsets.UTF_8), "AES");
                    cipher.init(Cipher.DECRYPT_MODE, keySpec);
                    msg = new String(cipher.doFinal(b), StandardCharsets.UTF_8);
                } catch (Exception e) {
                    try {
                        outputStream.write("2".getBytes(StandardCharsets.UTF_8));
                    } catch (IOException ex) {
                        LOGGER.error(ex.toString());
                    }
                    LOGGER.warn(e.toString());
                }
                if (!msg.startsWith("brc:")) {
                    try {
                        LOGGER.warn("Illegal BRC Request: {} from {}", msg, s.getRemoteAddress().toString());
                    } catch (IOException ignored) {
                        LOGGER.warn("Illegal BRC Request: {}", msg);
                    }
                    return;
                }
                if (!conversation.parse(msg.substring(4))) {
                    try {
                        outputStream.write("1".getBytes(StandardCharsets.UTF_8));
                    } catch (IOException ex) {
                        LOGGER.error(ex.toString());
                    }
                }
                try {
                    byte[] bytes = "0".getBytes(StandardCharsets.UTF_8);
                    outputStream.writeInt(bytes.length);
                    outputStream.write(bytes);
                    LOGGER.info("BRC Request: {} from {}", msg, s.getRemoteAddress().toString());
                } catch (IOException e) {
                    LOGGER.warn(e.toString());
                }
            }
        };
        AioQuickServer server = new AioQuickServer(port, new ByteArrayProtocol(), processor);
        server.start();
    }
}
