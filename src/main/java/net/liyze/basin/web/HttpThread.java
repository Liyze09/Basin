package net.liyze.basin.web;

import net.liyze.basin.core.Main;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class HttpThread extends Thread {
    protected Socket socket;
    private final Server server;

    public HttpThread(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        Main.LOGGER.trace("A HTTP link started");
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
            Main.LOGGER.error("HTTP Error: {}", e.toString());
        }
    }
}
