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

import static net.liyze.basin.core.Main.*;

public class Server {
    public static void server(@NotNull String token) throws Exception {
        final Conversation conversation = new Conversation();

        MessageProcessor<byte[]> processor = (s, b) -> {
            Cipher cipher;
            String msg = "";
            try {
                cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                SecretKey keySpec = new SecretKeySpec(cfg.accessToken.getBytes(StandardCharsets.UTF_8), "AES");
                cipher.init(Cipher.DECRYPT_MODE, keySpec);
                msg = new String(cipher.doFinal(b), StandardCharsets.UTF_8);
            } catch (Exception e) {
                LOGGER.info(e.toString());
            }
            LOGGER.info("Remote: {}", msg);
            if (!msg.startsWith("brc:")) {
                LOGGER.warn("Illegal BRC Request: {}", msg);
                return;
            }
            conversation.parse(msg.substring(4));
            try (WriteBuffer outputStream = s.writeBuffer()) {
                try {
                    byte[] bytes = "200".getBytes();
                    outputStream.writeInt(bytes.length);
                    outputStream.write(bytes);
                } catch (IOException e) {
                    LOGGER.info(e.toString());
                }
            }
        };
        AioQuickServer server = new AioQuickServer(600, new ByteArrayProtocol(), processor);
        server.start();
    }
}
