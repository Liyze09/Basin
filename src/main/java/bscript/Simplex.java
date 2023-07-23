package bscript;

import com.google.common.base.Splitter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class Simplex {
    private static final Logger LOGGER = LoggerFactory.getLogger("Simplex");
    public Path dir = Path.of("");
    public Project project = new Project();

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void init() throws IOException {
        dir.resolve("src").toFile().mkdirs();
        dir.resolve("build").toFile().mkdirs();
        dir.resolve("data").resolve("jars").toFile().mkdirs();
        Project cfg = new Project();
        File config = this.dir.resolve("simplex.json").toFile();
        if (!config.exists()) {
            try (Writer writer = Files.newBufferedWriter(config.toPath())) {
                config.createNewFile();
                var configObj = new Project();
                (new GsonBuilder().setPrettyPrinting().create()).toJson(configObj, writer);
                writer.flush();
            } catch (Exception e) {
                LOGGER.info("Error on loading config {}", e.toString());
            }
        }
        try (Reader reader = Files.newBufferedReader(config.toPath())) {
            cfg = new Gson().fromJson(reader, Project.class);
        } catch (IOException e) {
            LOGGER.info("Error on loading config {}", e.toString());
        }
        this.project = cfg;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void load() {
        HttpClient client = HttpClient.newBuilder().build();
        for (String dependency : this.project.dependencies) {
            Splitter splitter = Splitter.on(':');
            List<String> list = splitter.splitToList(dependency);
            AtomicBoolean completed = new AtomicBoolean(false);
            for (String repo : this.project.repositories) {
                Future<HttpResponse<byte[]>> r = client.sendAsync(
                        HttpRequest.newBuilder()
                                .uri(URI.create(repo + "/"
                                        + list.get(0).replace('.', '/') + "/"
                                        + list.get(1) + "/"
                                        + list.get(2) + "/"
                                        + list.get(1) + "-" + list.get(2) + ".jar"
                                ))
                                .timeout(Duration.ofSeconds(20))
                                .GET()
                                .build(), HttpResponse.BodyHandlers.ofByteArray());
                File file = new File("data/jars/".replace('/', File.separatorChar)
                        + list.get(1) + "-" + list.get(2) + ".jar");
                try {
                    file.createNewFile();
                    try (OutputStream out = new FileOutputStream(file)) {
                        out.write(r.get().body());
                        out.flush();
                    }
                    completed.set(true);
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                if (completed.get()) break;
            }
        }
    }

    public void compile() {

    }

    public static class Project {
        public String groupId = "";
        public String artifactId = "";
        public String[] repositories = {"https://repo1.maven.org/maven2"};
        public String[] dependencies = {};

        public String toString() {
            return groupId +
                    "." +
                    artifactId +
                    "repo:" +
                    Arrays.toString(repositories) +
                    "depend:" +
                    Arrays.toString(dependencies);
        }
    }
}