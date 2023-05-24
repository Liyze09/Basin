package net.liyze.basin.web;

import net.liyze.basin.core.Main;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class Server {
    @SuppressWarnings("unused")
    public static final Map<String, Function<String, byte[]>> dynamicFunctions = new HashMap<>();
    public static final Map<String, Server> runningServer = new HashMap<>();
    public String serverName;
    public int port;
    public final File root;
    public static ServerSocket server;
    public boolean isRunning = true;
    public static String index = "index.html";

    public Server(String name, int port) {
        this.port = port;
        this.serverName = name;
        root = new File("data/web/" + serverName + "/root");
        try {
            server = new ServerSocket(port);
            root.mkdirs();
        } catch (IOException e) {
            Main.LOGGER.error("Error: {}", e.toString());
        }
    }

    public void stop() {
        this.isRunning = false;
    }

    public Server run() throws IOException {
        AtomicReference<Socket> socket = new AtomicReference<>();
        Main.LOGGER.info("Server {} on port {} started", serverName, port);
        new Thread(() -> {
            while (isRunning) {
                try {
                    socket.set(server.accept());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                (new HttpThread(socket.get(), this)).start();
            }
            Main.LOGGER.info("Server {} on port {} stopped", serverName, port);
        }).start();
        return this;
    }
}
