package net.liyze.basin.remote;

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
    public static void send(String message, String host) throws Exception {
        MessageProcessor<String> processor = (session, msg) -> LOGGER.info("Receive from server: " + msg);
        AioQuickClient client = new AioQuickClient(host, 600, new StringProtocol(), processor);
        AioSession session = client.start();
        WriteBuffer writeBuffer = session.writeBuffer();
        byte[] msg = ("brc:" + message).getBytes(StandardCharsets.UTF_8);

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKey keySpec = new SecretKeySpec(envMap.get("\"" + host + "_token\"").toString().getBytes(StandardCharsets.UTF_8), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        msg = cipher.doFinal(msg);

        writeBuffer.writeInt(msg.length);
        writeBuffer.write(msg);
        writeBuffer.flush();
        LOGGER.info("Remote Sent: \"{}\" to \"{}\"", message, host);
        writeBuffer.close();
    }
}
