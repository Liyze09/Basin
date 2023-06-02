package net.liyze.basin.remote;

import org.smartboot.socket.MessageProcessor;
import org.smartboot.socket.extension.protocol.StringProtocol;
import org.smartboot.socket.transport.AioQuickClient;
import org.smartboot.socket.transport.AioSession;
import org.smartboot.socket.transport.WriteBuffer;

public class Client {
    public static void send(String message, String host) throws Exception {
        MessageProcessor<String> processor = (session, msg) -> System.out.println("receive from server: " + msg);
        AioQuickClient client = new AioQuickClient(host, 600, new StringProtocol(), processor);
        AioSession session = client.start();
        try (WriteBuffer writeBuffer = session.writeBuffer()) {
            byte[] data = message.getBytes();
            writeBuffer.writeInt(data.length);
            writeBuffer.write(data);
            writeBuffer.flush();
        }
    }
}
