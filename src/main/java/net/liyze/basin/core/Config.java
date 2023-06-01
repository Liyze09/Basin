package net.liyze.basin.core;

import com.google.gson.GsonBuilder;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;

import static net.liyze.basin.core.Main.config;

public final class Config {

    public int taskPoolSize = Runtime.getRuntime().availableProcessors() + 1;
    public boolean doLoadJars = true;
    public String startCommand = "";

    static Config initConfig() throws Exception {
        Config cfg;
        if (!config.exists()) {
            try (Writer writer = Files.newBufferedWriter(config.toPath());) {
                (new GsonBuilder().setPrettyPrinting().create()).toJson(config, writer);
            }
        }
        try (Reader reader = Files.newBufferedReader(config.toPath())) {
            cfg = (new GsonBuilder().setPrettyPrinting().create()).fromJson(reader, Config.class);
        }
        return cfg;
    }
}
