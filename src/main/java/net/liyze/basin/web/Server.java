package net.liyze.basin.web;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static net.liyze.basin.Main.LOGGER;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class Server {
    public String serverName;
    public int port;
    public final File root;
    public static ServerSocket server;
    public static String index = "index.html";

    public Server(String name, int port) {
        this.port = port;
        this.serverName = name;
        root = new File("data/web/" + serverName + "/root");
        try {
            server = new ServerSocket(port);
            root.mkdirs();
        } catch (IOException e) {
            LOGGER.error("Error: {}", e.toString());
        }
    }

    @SuppressWarnings("InfiniteLoopStatement")
    public void run() throws Exception {
        Socket socket;
        for (; ; ) {
            socket = server.accept();
            (new HTTP(socket, this)).start();
        }
    }
}
