package net.liyze.basin.core;

import com.google.gson.GsonBuilder;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;

import static net.liyze.basin.core.Main.config;

public class Config {
    public static Config cfg = new Config();
    public int taskPoolSize = Runtime.getRuntime().availableProcessors() + 1;
    public int webStreamCapacity = 2048;
    public boolean debug = false;

    static void initConfig() throws Exception {
        if (!config.exists()) {
            String gson = (new GsonBuilder().setPrettyPrinting().create()).toJson(cfg);
            try (Writer writer = new FileWriter(config.getPath())) {
                writer.write(gson);
            }
        }
        try (Reader reader = new FileReader(config.getPath())) {
            cfg = (new GsonBuilder().setPrettyPrinting().create()).fromJson(reader, Config.class);
        }
    }
}
