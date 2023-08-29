@file: JvmName("Client")

package net.liyze.basin.core.remote

import net.liyze.basin.core.cfg
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.smartboot.socket.MessageProcessor
import org.smartboot.socket.extension.protocol.StringProtocol
import org.smartboot.socket.transport.AioQuickClient
import org.smartboot.socket.transport.AioSession
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

val LOGGER: Logger = LoggerFactory.getLogger("BRCClient")

@JvmOverloads
@Throws(Exception::class)
fun send(message: String, host: String, token: String, port: Int = cfg.remotePort) {
    val processor = MessageProcessor { _: AioSession, msg: String ->
        LOGGER.info(
            "Receive from server: $msg"
        )
    }
    val client = AioQuickClient(host, port, StringProtocol(), processor)
    val session = client.start()
    val writeBuffer = session.writeBuffer()
    var msg = "brc:$message".toByteArray(StandardCharsets.UTF_8)
    val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
    val keySpec: SecretKey = SecretKeySpec(token.toByteArray(StandardCharsets.UTF_8), "AES")
    cipher.init(Cipher.ENCRYPT_MODE, keySpec)
    msg = cipher.doFinal(msg)
    writeBuffer.writeInt(msg.size)
    writeBuffer.write(msg)
    writeBuffer.flush()
    LOGGER.info("Remote Sent: \"{}\" to \"{}\":{}", message, host, port)
    client.shutdown()
}
