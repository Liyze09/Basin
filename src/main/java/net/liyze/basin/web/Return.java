package net.liyze.basin.web;

import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.liyze.basin.Config.cfg;
import static net.liyze.basin.Main.LOGGER;
import static net.liyze.basin.web.Server.dynamicFunctions;

@SuppressWarnings("unused")
public class Return {
    private final OutputStream out;
    private final Server server;
    private final int streamCapacity = cfg.webStreamCapacity;
    private String type = "text/html";

    public Return(OutputStream out, Server server) {
        this.out = out;
        this.server = server;
    }

    public Return setType(String type) {
        this.type = type;
        return this;
    }

    @SuppressWarnings("ConstantValue")
    public void send(String page) throws IOException {
        byte[] data = new byte[streamCapacity];
        AtomicBoolean isStatic = new AtomicBoolean(true);
        dynamicFunctions.forEach((name, func) -> {
            if (page.matches(name)) {
                byte[] bytes = func.apply(page);
                if (page != null) {
                    try {
                        out.write(bytes);
                        isStatic.set(false);
                    } catch (IOException e) {
                        LOGGER.error("HTTP Dynamic Error: {}", e.toString());
                    }
                }
            }
        });
        if (isStatic.get()) {
            File file = new File(server.root, page);
            if (file.exists()) {
                try (FileInputStream reader = new FileInputStream(file)) {
                    int x = reader.read(data, 0, streamCapacity);
                    String msg = " HTTP/1.1 200 OK\r\n" +
                            "Content-Type:" + type +
                            "\r\nContent-Length:" + x + "\r\n\r\n";
                    byte[] rdata = ArrayUtils.addAll(msg.getBytes(StandardCharsets.UTF_8), data);
                    out.write(rdata);
                }
            } else {
                String msg = """
                        HTTP/1.1 404 File Not Found\r
                        Content-Type:text/html\r
                        Content-Length:23\r
                        \r
                        <h1> 404 File Not Found </h1>
                        """;
                out.write(msg.getBytes(StandardCharsets.UTF_8));
            }
        }
        out.close();
    }
}
