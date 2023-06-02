package net.liyze.basin.remote;

import net.liyze.basin.core.Conversation;
import org.smartboot.socket.MessageProcessor;
import org.smartboot.socket.extension.protocol.StringProtocol;
import org.smartboot.socket.transport.AioQuickServer;
import org.smartboot.socket.transport.WriteBuffer;

import java.io.IOException;

import static net.liyze.basin.core.Main.LOGGER;

public class Server {
    public static void server() throws IOException {
        final Conversation conversation = new Conversation();
        MessageProcessor<String> processor = (session, msg) -> {
            LOGGER.info("Remote: {}", msg);
            conversation.parse(msg);
            try (WriteBuffer outputStream = session.writeBuffer()) {
                try {
                    byte[] bytes = "OK".getBytes();
                    outputStream.writeInt(bytes.length);
                    outputStream.write(bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        AioQuickServer server = new AioQuickServer(600, new StringProtocol(), processor);
        server.start();
    }
}
