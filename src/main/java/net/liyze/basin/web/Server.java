package net.liyze.basin.web;

import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.server.HttpBootstrap;
import org.smartboot.http.server.HttpRequest;
import org.smartboot.http.server.HttpResponse;
import org.smartboot.http.server.HttpServerHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static net.liyze.basin.core.Main.LOGGER;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class Server {
    @SuppressWarnings("unused")
    public static final Map<String, Function<String, byte[]>> dynamicFunctions = new HashMap<>();
    public static final Map<String, Server> runningServer = new HashMap<>();
    public final File root;
    public String serverName;
    public int port;
    private final HttpBootstrap bootstrap = new HttpBootstrap();

    public Server(String name, int port) {
        this.port = port;
        this.serverName = name;
        root = new File("data/web/" + serverName + "/root");
        root.mkdirs();
    }

    public void stop() {
        bootstrap.shutdown();
        LOGGER.info("Server {} on port {} stopped", serverName, port);
    }

    public Server run() throws IOException {
        LOGGER.info("Server {} on port {} started", serverName, port);
        try {
            ServerSocket socket = new ServerSocket(port);
            socket.close();
            bootstrap.configuration().serverName(serverName);
            bootstrap.httpHandler(new HttpServerHandler() {
                @Override
                public void handle(HttpRequest request, HttpResponse response) {
                    String uri = request.getRequestURI();
                    String type = request.getContentType();
                    if (uri.equals("/") || uri.isBlank()) {
                        uri = "index.html";
                    }
                    String finalUri = uri;
                    AtomicBoolean isStatic = new AtomicBoolean(true);
                    dynamicFunctions.forEach((r, f) -> {
                        if (finalUri.matches(r)) {
                            try {
                                byte[] bytes = f.apply(finalUri);
                                if (bytes != null) {
                                    response.write(bytes);
                                    isStatic.set(false);
                                }
                            } catch (IOException e) {
                                LOGGER.info("HTTP Error {}", e.toString());
                            }
                        }
                    });
                    if (!isStatic.get()) return;
                    File file = new File(root + File.separator + uri);
                    if (!file.exists()) {
                        file = new File(root + File.separator + "404.html");
                        response.setHttpStatus(HttpStatus.NOT_FOUND);
                    }
                    if (type == null) {
                        if (file.toString().endsWith(".html")) {
                            response.setContentType("text/html");
                        } else if (file.toString().endsWith(".mp4")) {
                            response.setContentType("video/mp4");
                        } else if (file.toString().endsWith(".ogg")) {
                            response.setContentType("video/ogg");
                        } else if (file.toString().endsWith(".webm")) {
                            response.setContentType("video/webm");
                        } else if (file.toString().endsWith(".mp3")) {
                            response.setContentType("audio/mpeg");
                        } else if (file.toString().endsWith(".wav")) {
                            response.setContentType("audio/wav");
                        } else if (file.toString().endsWith(".css")) {
                            response.setContentType("text/css");
                        }
                    } else {
                        response.setContentType(type);
                    }
                    try (InputStream stream = new FileInputStream(file)) {
                        byte[] bytes = stream.readAllBytes();
                        response.setContentLength(bytes.length);
                        response.write(bytes);
                    } catch (IOException e) {
                        LOGGER.info("HTTP Error {}", e.toString());
                        try {
                            response.write("<h1>  500 Internal Server Error  </h1>".getBytes(StandardCharsets.UTF_8));
                        } catch (IOException ex) {
                            LOGGER.info("HTTP Error {}", e.toString());
                        }
                    }
                }
            }).setPort(port).start();
        } catch (Exception e) {
            LOGGER.error("Port {} is using!", port);
        }
        return this;
    }
}