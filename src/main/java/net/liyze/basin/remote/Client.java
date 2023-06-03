package net.liyze.basin.remote;

import org.smartboot.socket.MessageProcessor;
import org.smartboot.socket.extension.protocol.StringProtocol;
import org.smartboot.socket.transport.AioQuickClient;
import org.smartboot.socket.transport.AioSession;
import org.smartboot.socket.transport.WriteBuffer;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import static net.liyze.basin.core.Main.*;

public class Client {
    public static void send(String message, String host) throws Exception {
        MessageProcessor<String> processor = (session, msg) -> LOGGER.info("Receive from server: " + msg);
        AioQuickClient client = new AioQuickClient(host, 600, new StringProtocol(), processor);
        AioSession session = client.start();
        WriteBuffer writeBuffer = session.writeBuffer();
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(envMap.get(host + "_token").toString().getBytes(StandardCharsets.UTF_8));
        byte[] msg = ("brc:#" + toHex(md.digest()) + "#" + message).getBytes(StandardCharsets.UTF_8);
        writeBuffer.writeInt(msg.length);
        writeBuffer.write(msg);
        writeBuffer.flush();
        LOGGER.info("Remote Sent: \"{}\" to \"{}\"", message, host);
        writeBuffer.close();
    }

    static String toHex(byte[] bytes) {
        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        char[] resultCharArray = new char[bytes.length * 2];
        int index = 0;
        for (byte b : bytes) {
            resultCharArray[index++] = hexDigits[b >>> 4 & 0xf];
            resultCharArray[index++] = hexDigits[b & 0xf];
        }
        return new String(resultCharArray);
    }
}
