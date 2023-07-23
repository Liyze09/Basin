package bscript;

import com.google.common.base.Splitter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartboot.http.client.HttpClient;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Simplex {
    private static final Logger LOGGER = LoggerFactory.getLogger("Simplex");
    public Path dir;
    public Project project = new Project();

    public void init(String dir) throws IOException {
        init(Path.of(dir));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void init(@NotNull Path dir) throws IOException {
        this.dir = dir;
        dir.resolve("src").toFile().mkdirs();
        dir.resolve("build").toFile().mkdirs();
        dir.resolve("data").resolve("jars").toFile().mkdirs();
        project.init();
    }

    public void load() {
        for (String dependency : this.project.dependencies) {
            Splitter splitter = Splitter.on(':');
            List<String> list = splitter.splitToList(dependency);
            for (String repo : this.project.repositories) {
                try {
                    URL url = new URI(repo).toURL();
                    new HttpClient(url.getHost(), 80)
                            .get(url.getPath()
                                    + list.get(0).replace('.', '/')
                                    + "/" + list.get(1)
                                    + "/" + list.get(2)
                                    + "/" + list.get(1) + "-" + list.get(2) + ".jar"
                            ).onSuccess(httpResponse -> {

                            });
                } catch (MalformedURLException | URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public final class Project {
        public String groupId = "";
        public String artifactId = "";
        public String[] repositories = {};
        public String[] dependencies = {};

        @SuppressWarnings("ResultOfMethodCallIgnored")
        void init() {
            Project cfg = null;
            File config = Simplex.this.dir.resolve("simplex.json").toFile();
            if (!config.exists()) {
                try (Writer writer = Files.newBufferedWriter(config.toPath())) {
                    config.createNewFile();
                    var configObj = new Project();
                    (new GsonBuilder().setPrettyPrinting().create()).toJson(configObj, writer);
                } catch (Exception e) {
                    LOGGER.info("Error on loading config {}", e.toString());
                }
            }
            try (Reader reader = Files.newBufferedReader(config.toPath())) {
                cfg = new Gson().fromJson(reader, Project.class);
            } catch (IOException e) {
                LOGGER.info("Error on loading config {}", e.toString());
            }
            Simplex.this.project = cfg;
        }
    }
}
