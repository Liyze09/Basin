package net.liyze.basin.web;

import net.liyze.basin.context.BeanDefinition;
import net.liyze.basin.web.annotation.GetMapping;
import net.liyze.basin.web.annotation.PostMapping;
import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.server.HttpBootstrap;
import org.smartboot.http.server.HttpRequest;
import org.smartboot.http.server.HttpResponse;
import org.smartboot.http.server.HttpServerHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static net.liyze.basin.core.Main.LOGGER;
import static net.liyze.basin.core.Main.contexts;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class Server {
    @SuppressWarnings("unused")

    public static final Map<String, Server> runningServer = new HashMap<>();
    public final File root;
    private final HttpBootstrap bootstrap = new HttpBootstrap();
    public String serverName;
    public int port;

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
        final List<BeanDefinition> models = new ArrayList<>();
        contexts.forEach(i -> models.addAll(i.findBeanDefinitions(Model.class)));
        try {
            bootstrap.configuration().serverName(serverName);
            bootstrap.httpHandler(new HttpServerHandler() {
                @Override
                public void handle(HttpRequest request, HttpResponse response) {
                    LOGGER.debug("A HTTP Link {} started from {}", request.getRequestURL(), request.getRemoteHost());
                    String uri = request.getRequestURI();
                    String type = request.getContentType();
                    if (uri.equals("/") || uri.isBlank()) {
                        uri = "index.html";
                    }
                    final String finalUri = uri;
                    AtomicReference<View> view = new AtomicReference<>(null);
                    if (request.getMethod().equalsIgnoreCase("get")) {
                        models.forEach(bean -> List.of(bean.getBeanClass().getMethods()).forEach(m -> {
                            if (m.getAnnotation(GetMapping.class) != null) {
                                if (m.getAnnotation(GetMapping.class).path().startsWith(finalUri)) {
                                    try {
                                        view.set((View) m.invoke(bean.getInstance(), finalUri));
                                    } catch (Exception e) {
                                        LOGGER.error(e.toString());
                                    }
                                }
                            }
                        }));
                    } else {
                        models.forEach(bean -> List.of(bean.getBeanClass().getMethods()).forEach(m -> {
                            if (m.getAnnotation(PostMapping.class) != null) {
                                if (m.getAnnotation(PostMapping.class).path().startsWith(finalUri)) {
                                    try {
                                        view.set((View) m.invoke(bean.getInstance(), finalUri));
                                    } catch (Exception e) {
                                        LOGGER.error(e.toString());
                                    }
                                }
                            }
                        }));
                    }
                    if (view.get() != null) {
                        byte[] r = view.get().getPage(request);
                        response.setContentLength(r.length);
                        try {
                            response.write(r);
                        } catch (IOException e) {
                            LOGGER.error(e.toString());
                            return;
                        }
                    }
                    File file = new File(root + File.separator + uri.replace('/', File.separatorChar));
                    if (!file.exists()) {
                        if (this.getClass().getResource("/static/" + root + uri) == null) {
                            file = new File(root + File.separator + "404.html");
                            response.setHttpStatus(HttpStatus.NOT_FOUND);
                            if (!file.exists()) {
                                try {
                                    //noinspection DataFlowIssue
                                    file = new File(this.getClass().getResource("/static/404.html").toURI());
                                } catch (URISyntaxException e) {
                                    LOGGER.error(e.toString());
                                }
                            }
                        } else {
                            try {
                                //noinspection DataFlowIssue
                                file = new File(this.getClass().getResource("/static/" + root + uri).toURI());
                            } catch (URISyntaxException e) {
                                LOGGER.error(e.toString());
                            }
                        }
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
                        LOGGER.warn("HTTP Error {}", e.toString());
                        try {
                            response.write("<body style=\"background:grey;\"><h1>500 Internal Server Error</h1></body>".getBytes(StandardCharsets.UTF_8));
                        } catch (IOException ex) {
                            LOGGER.warn("HTTP Error {}", e.toString());
                        }
                    }
                }
            }).setPort(port).start();
        } catch (Exception e) {
            LOGGER.error(e.toString());
        }
        return this;
    }
}