package net.liyze.basin.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import static net.liyze.basin.Main.LOGGER;

public class HttpThread extends Thread {
    protected Socket socket;
    private final Server server;

    public HttpThread(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        LOGGER.trace("A HTTP link started");
        InputStream in;
        OutputStream out;
        try {
            in = socket.getInputStream();
            out = socket.getOutputStream();
            Receive receive = new Receive(in);
            String URL = receive.get();
            if ("/".equals(URL)) {
                URL = Server.index;
            }
            Return back = new Return(out, server);
            if (URL != null) back.send(URL);
        } catch (IOException e) {
            LOGGER.error("HTTP Error: {}", e.toString());
        }
    }
}
