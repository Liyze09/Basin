package net.liyze.basin.http;

import net.liyze.basin.context.BeanDefinition;
import net.liyze.basin.core.Server;
import net.liyze.basin.http.annotation.GetMapping;
import net.liyze.basin.http.annotation.Model;
import net.liyze.basin.http.annotation.PostMapping;
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
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static net.liyze.basin.core.Main.*;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class HttpServer implements Server {
    public static final Map<String, HttpServer> runningServer = new HashMap<>();
    public final File root;
    private final HttpBootstrap bootstrap = new HttpBootstrap();
    public String serverName;
    public int port;

    /**
     * Init server on {@code <port>} with {@code <name>}
     */
    public HttpServer(String name, int port) {
        this.port = port;
        this.serverName = name;
        root = new File("data/web/" + serverName + "/root");
        root.mkdirs();
    }

    /**
     * Stop web server.
     */
    public void stop() {
        bootstrap.shutdown();
        LOGGER.info("Server {} on port {} stopped", serverName, port);
    }

    /**
     * Start web server.
     */
    @SuppressWarnings("DataFlowIssue")
    public HttpServer start() {
        LOGGER.info("Server {} on port {} started", serverName, port);
        final List<BeanDefinition> models = new ArrayList<>();
        contexts.forEach(i -> models.addAll(i.getBeanDefinitions().stream().filter(bean -> bean.getBeanClass().isAnnotationPresent(Model.class)).toList()));
        try {
            bootstrap.configuration().serverName(serverName);
            bootstrap.httpHandler(new HttpServerHandler() {
                @Override
                public void handle(HttpRequest request, HttpResponse response) {
                    LOGGER.debug("A HTTP Link {} started from {}", request.getRequestURL(), request.getRemoteHost());
                    LOGGER.trace("Request:\nMethod: {}\nContentType: {}\nCharacterEncoding: {}\nProtocol: {}\nScheme: {}",
                            request.getMethod(),
                            request.getContentType(),
                            request.getCharacterEncoding(),
                            request.getProtocol(),
                            request.getScheme());
                    String uri = request.getRequestURI();
                    if (uri.equals("/") || uri.isBlank()) {
                        uri = "/index.html";
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
                    File file = new File(root + uri.replace('/', File.separatorChar));
                    if (!file.exists()) {
                        if (this.getClass().getResource("/static/" + root + uri) == null && (this.getClass().getResource("/static/" + uri) == null)) {
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
                                if (this.getClass().getResource("/static/" + root + uri) != null) {
                                    file = new File(this.getClass().getResource("/static/" + root + uri).toURI());
                                } else {
                                    file = new File(this.getClass().getResource("/static" + uri).toURI());
                                }
                            } catch (URISyntaxException e) {
                                LOGGER.error(e.toString());
                            }
                        }
                    }
                    if (request.getContentType() != null)
                        response.setContentType(request.getContentType());
                    else try {
                        response.setContentType(Files.probeContentType(file.toPath()));
                    } catch (IOException e) {
                        LOGGER.error(e.toString());
                    }
                    try (InputStream stream = new FileInputStream(file)) {
                        byte[] bytes = stream.readAllBytes();
                        response.setContentLength(bytes.length);
                        response.write(bytes);
                    } catch (IOException e) {
                        LOGGER.warn("HTTP Error {}", e.toString());
                        try {
                            response.write("<html><body><h1 style=\"font-family:arial;font-size:64px;text-align:center;\">500 Internal Server Error</h1></body></html>".getBytes(StandardCharsets.UTF_8));
                        } catch (IOException ex) {
                            LOGGER.warn("HTTP Error {}", e.toString());
                        }
                    }
                    LOGGER.trace("Response:\nStatus: {} {}\nContentType: {}",
                            response.getHttpStatus(),
                            response.getReasonPhrase(),
                            response.getContentType()
                    );
                }
            }).setPort(port).start();
        } catch (Exception e) {
            LOGGER.error(e.toString());
        }
        return this;
    }
}