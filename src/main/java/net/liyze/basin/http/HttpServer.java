package net.liyze.basin.http;

import com.google.gson.Gson;
import freemarker.cache.FileTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import net.liyze.basin.core.Basin;
import net.liyze.basin.core.Server;
import net.liyze.basin.http.annotation.GetMapping;
import net.liyze.basin.http.annotation.PostMapping;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.server.HttpBootstrap;
import org.smartboot.http.server.HttpRequest;
import org.smartboot.http.server.HttpResponse;
import org.smartboot.http.server.HttpServerHandler;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class HttpServer implements Server {
    private final Configuration FREE_MARKER = new Configuration(Configuration.VERSION_2_3_32);
    private final HttpBootstrap bootstrap = new HttpBootstrap();
    public final int port;
    public final String serverName;
    public final File root;
    public Map<String, GetDispatcher> getMappings = new HashMap<>();
    public Map<String, PostDispatcher> postMappings = new HashMap<>();
    public Gson gson = new Gson();

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public HttpServer(String name, int port) throws IOException {
        this.port = port;
        this.serverName = name;
        root = new File(("data/web/" + serverName + "/root").replace('/', File.separatorChar));
        root.mkdirs();
        var temp = Path.of(root.getPath()).resolve("template").toFile();
        temp.mkdirs();
        FREE_MARKER.setTemplateLoader(
                new FileTemplateLoader(temp));
        Basin.contexts.forEach(context -> context.getBeans(WebController.class)
                .forEach(bean -> Arrays.stream(bean.getClass().getMethods())
                        .forEach(method -> {
                            GetMapping annotation = method.getAnnotation(GetMapping.class);
                            if (annotation != null) {
                                getMappings.put(annotation.path(), new GetDispatcher(bean, method, method.getParameterTypes()));
                            }
                            PostMapping annotation1 = method.getAnnotation(PostMapping.class);
                            if (annotation1 != null) {
                                postMappings.put(annotation1.path(), new PostDispatcher(bean, method, method.getParameterTypes(), gson));
                            }
                        }))
        );
    }

    @Override
    public void stop() {
        bootstrap.shutdown();
    }

    @Override
    public @NotNull Server start() {
        bootstrap.configuration().serverName(serverName);
        bootstrap.httpHandler(new HttpServerHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response) throws IOException {
                try {
                    if (request.getMethod().equalsIgnoreCase("get")) {
                        getDispatch(request, response);
                    } else if (request.getMethod().equalsIgnoreCase("post")) {
                        postDispatch(request, response);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    response.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
                    getFileResource("/500.html", response);
                }
            }
        }).setPort(port).start();
        return this;
    }

    private void getDispatch(@NotNull HttpRequest request, HttpResponse response) throws InvocationTargetException, IllegalAccessException, IOException, TemplateException {
        if (request.getRequestURI().startsWith("/favicon.ico") || request.getRequestURI().startsWith("/static")) {
            staticResource(request, response);
            return;
        }
        GetDispatcher dispatcher = getGetDispatcher(request.getRequestURI());
        if (dispatcher == null) {
            staticResource(request, response);
            return;
        }
        ModelAndView view = dispatcher.invoke(request, response);
        if (view != null) {
            if (view.view().startsWith("redirect:")) {
                response.setHeader("Location", view.view().substring(9).strip());
                if (response.getHttpStatus() == 200) {
                    response.setHttpStatus(HttpStatus.TEMPORARY_REDIRECT);
                }
            }
            render(view, response);
        }
    }

    private void render(@NotNull ModelAndView view, @NotNull HttpResponse response) throws IOException, TemplateException {
        var template = FREE_MARKER.getTemplate(view.view());
        var out = new StringWriter();
        template.process(view.model(), out);
        response.write(out.getBuffer().toString().getBytes(StandardCharsets.UTF_8));
    }

    private void postDispatch(@NotNull HttpRequest request, HttpResponse response) throws InvocationTargetException, IllegalAccessException, TemplateException, IOException {
        PostDispatcher dispatcher = getPostDispatcher(request.getRequestURI());
        if (dispatcher == null) {
            response.setHttpStatus(HttpStatus.NOT_FOUND);
            getFileResource("/404.html", response);
            return;
        }
        ModelAndView view = dispatcher.invoke(request, response);
        if (view != null) {
            if (view.view().startsWith("redirect:")) {
                response.setHeader("Location", view.view().substring(9).strip());
                if (response.getHttpStatus() == 200) {
                    response.setHttpStatus(HttpStatus.TEMPORARY_REDIRECT);
                }
            }
            render(view, response);
        }
    }

    private @Nullable GetDispatcher getGetDispatcher(String uri) {
        GetDispatcher result = getMappings.get(uri);
        if (!uri.contains("/")) return null;
        if (result == null) {
            result = getGetDispatcher(uri.substring(0, uri.lastIndexOf("/")));
        }
        return result;
    }

    private @Nullable PostDispatcher getPostDispatcher(String uri) {
        PostDispatcher result = postMappings.get(uri);
        if (!uri.contains("/")) return null;
        if (result == null) {
            result = getPostDispatcher(uri.substring(0, uri.lastIndexOf("/")));
        }
        return result;
    }

    private void staticResource(@NotNull HttpRequest request, HttpResponse response) throws IOException {
        getFileResource(request.getRequestURI(), response);
    }

    private void getFileResource(@NotNull String uri, HttpResponse response) throws IOException {
        if (uri.equals("/") || uri.isBlank()) uri = "/index.html";
        try (InputStream tmp0 = HttpServer.class.getResourceAsStream("/static/" + serverName + uri);
             InputStream tmp1 = HttpServer.class.getResourceAsStream("/static" + uri)) {
            File tmp2 = new File(
                    "data" + File.separator + "web" + File.separator
                            + serverName + uri.replace('/', File.separatorChar));
            if (tmp0 != null) {
                response.write(tmp0.readAllBytes());
            } else if (tmp2.exists()) {
                try (InputStream input = new FileInputStream(tmp2)) {
                    response.write(input.readAllBytes());
                }
            } else if (tmp1 != null) {
                response.write(tmp1.readAllBytes());
            } else {
                response.setHttpStatus(HttpStatus.NOT_FOUND);
                getFileResource("/404.html", response);
            }
        }
    }
}
