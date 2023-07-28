package bscript;

import com.google.common.base.Splitter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
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
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public final class Simplex {
    private static final Logger LOGGER = LoggerFactory.getLogger("Simplex");
    public Path dir = Path.of("");
    public Project project = new Project();

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void init() {
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
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Contract
    public @NotNull Map<String, byte[]> compile() {
        final ArrayList<File> files = new ArrayList<>();
        File src = dir.resolve("src").toFile();
        Map<String, byte[]> bytes = BScriptHelper.getInstance().compileFilesToBytes(files(Arrays.stream(Objects.requireNonNull(src.listFiles())).toList()));
        for (Map.Entry<String, byte[]> entry : bytes.entrySet()) {
            var file = new File("build" + File.separator + entry.getKey().replace('.', File.separatorChar) + ".class");
            try (OutputStream stream = new FileOutputStream(file)) {
                file.createNewFile();
                stream.write(entry.getValue());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return bytes;
    }

    public void build() {
        var jar = new BScriptJarBuilder();
        jar.toJar(compile(), "build/libs/" + project.artifactId + "-" + project.version + ".jar");
    }

    private @NotNull List<File> files(@NotNull List<File> files) {
        List<File> ret = new ArrayList<>();
        files.forEach(file -> {
            if (file.isFile()) {
                ret.add(file);
            } else {
                ret.addAll(files(Arrays.stream(Objects.requireNonNull(file.listFiles())).toList()));
            }
        });
        return ret;
    }

    public static class Project {
        public String groupId = "";
        public String artifactId = "";
        public String version = "1.0-SNAPSHOT";
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

    public static class MavenProject {
        public List<List<String>> dependencies;
    }
}