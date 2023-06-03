package net.liyze.basin.remote;

import net.liyze.basin.core.Conversation;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.smartboot.socket.MessageProcessor;
import org.smartboot.socket.extension.protocol.StringProtocol;
import org.smartboot.socket.transport.AioQuickServer;
import org.smartboot.socket.transport.WriteBuffer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import static net.liyze.basin.core.Main.LOGGER;
import static net.liyze.basin.remote.Client.toHex;

public class Server {
    public static void server(@NotNull String token) throws Exception {
        final Conversation conversation = new Conversation();
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(token.getBytes(StandardCharsets.UTF_8));
        String tokenHex = toHex(md.digest());
        MessageProcessor<String> processor = (session, msg) -> {
            LOGGER.info("Remote: {}", msg);
            String[] request = StringUtils.split(msg, '#');
            if (request.length != 3 || !request[0].equals("brc:") || !request[1].equals(tokenHex)) {
                LOGGER.warn("Illegal BRC Request: {}", msg);
                return;
            }
            conversation.parse(request[2]);
            try (WriteBuffer outputStream = session.writeBuffer()) {
                try {
                    byte[] bytes = "200".getBytes();
                    outputStream.writeInt(bytes.length);
                    outputStream.write(bytes);
                } catch (IOException e) {
                    LOGGER.info(e.toString());
                }
            }
        };
        AioQuickServer server = new AioQuickServer(600, new StringProtocol(), processor);
        server.start();
    }
}
