package net.liyze.basin.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;

import static net.liyze.basin.core.Main.LOGGER;
import static net.liyze.basin.core.Main.config;

public final class Config {

    public int taskPoolSize = Runtime.getRuntime().availableProcessors() + 1;
    public boolean doLoadJars = true;
    public String startCommand = "";

    static Config initConfig() {
        Config cfg = new Config();
        if (!config.exists()) {
            try (Writer writer = Files.newBufferedWriter(config.toPath())) {
                (new GsonBuilder().setPrettyPrinting().create()).toJson(config, writer);
            } catch (IOException e) {
                LOGGER.info("Error on loading config {}", e.toString());
            }
        }
        try (Reader reader = Files.newBufferedReader(config.toPath())) {
            cfg = new Gson().fromJson(reader, Config.class);
        } catch (IOException e) {
            LOGGER.info("Error on loading config {}", e.toString());
        }
        return cfg;
    }
}
