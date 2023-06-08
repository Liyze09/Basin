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
import java.util.ArrayList;
import java.util.List;

import static net.liyze.basin.core.Main.LOGGER;

public class Server {
    public static final List<Server> servers = new ArrayList<>();
    private final String token;
    private final int port;
    private final Conversation REMOTE_CONVERSATION;
    public AioQuickServer server = null;

    public Server(@NotNull String token, int port, Conversation remoteConversation) {
        servers.add(this);
        this.token = token;
        this.port = port;
        REMOTE_CONVERSATION = remoteConversation;
    }

    public void shutdown() {
        server.shutdown();
    }

    public void start() throws Exception {
        MessageProcessor<byte[]> processor = (s, b) -> {
            Cipher cipher;
            String msg = "";

            WriteBuffer outputStream = s.writeBuffer();
            try {
                cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                SecretKey keySpec = new SecretKeySpec(token.getBytes(StandardCharsets.UTF_8), "AES");
                cipher.init(Cipher.DECRYPT_MODE, keySpec);
                msg = new String(cipher.doFinal(b), StandardCharsets.UTF_8);
            } catch (Exception e) {
                try {
                    outputStream.write("Illegal BRC Request.".getBytes(StandardCharsets.UTF_8));
                } catch (IOException ex) {
                    LOGGER.error(ex.toString());
                }
                LOGGER.warn(e.toString());
            }
            if (!msg.startsWith("brc:")) {
                try {
                    byte[] bytes = "Illegal BRC Request.".getBytes(StandardCharsets.UTF_8);
                    LOGGER.warn("Illegal BRC Request: {} from {}", msg, s.getRemoteAddress().toString());
                    outputStream.writeInt(bytes.length);
                    outputStream.write(bytes);
                } catch (IOException e) {
                    LOGGER.warn("Illegal BRC Request: {}", msg);
                }
                return;
            }
            if (!REMOTE_CONVERSATION.parse(msg.substring(4))) {
                try {
                    byte[] bytes = "Failed to run the command.".getBytes(StandardCharsets.UTF_8);
                    outputStream.writeInt(bytes.length);
                    outputStream.write(bytes);
                } catch (IOException ex) {
                    LOGGER.error(ex.toString());
                }
            } else {
                try {
                    byte[] bytes = "Okay".getBytes(StandardCharsets.UTF_8);
                    outputStream.writeInt(bytes.length);
                    outputStream.write(bytes);
                    LOGGER.info("BRC Request: {} from {}", msg, s.getRemoteAddress().toString());
                } catch (IOException e) {
                    LOGGER.warn(e.toString());
                }
            }

        };
        server = new AioQuickServer(port, new ByteArrayProtocol(), processor);
        server.setLowMemory(true);
        server.start();
    }
}
