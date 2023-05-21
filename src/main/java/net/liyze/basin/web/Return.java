package net.liyze.basin.web;

import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static net.liyze.basin.Config.cfg;

public class Return {
    private final OutputStream out;
    private final Server server;
    private final int streamCapacity = cfg.webStreamCapacity;

    public Return(OutputStream out, Server server) {
        this.out = out;
        this.server = server;
    }

    public void send(String page) throws IOException {
        byte[] data = new byte[streamCapacity];
        File file = new File(server.root, page);
        if (file.exists()) {
            try (FileInputStream reader = new FileInputStream(file)) {
                int x = reader.read(data, 0, streamCapacity);
                String msg = """
                        HTTP/1.1 200 OK\r
                        Content-Type:text/html\r
                        Content-Length:""" + x + "\r\n\r\n";
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
        out.close();
    }
}
