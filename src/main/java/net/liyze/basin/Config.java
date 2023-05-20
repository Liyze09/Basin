package net.liyze.basin;

import com.google.gson.GsonBuilder;

import java.io.*;

import static net.liyze.basin.Main.*;

public class Config {
    public static Config cfg = new Config();
    public int taskPoolSize = Runtime.getRuntime().availableProcessors() + 1;

    static void initConfig() {
        if (!config.exists()) {
            String gson = (new GsonBuilder().setPrettyPrinting().create()).toJson(cfg);
            try (Writer writer = new FileWriter(config.getPath())) {
                writer.write(gson);
            } catch (IOException e) {
                LOGGER.error("Error when create config file: ", e);
            }
        }
        try (Reader reader = new FileReader(config.getPath())) {
            cfg = (new GsonBuilder().setPrettyPrinting().create()).fromJson(reader, Config.class);
        } catch (IOException e) {
            LOGGER.error("Error when load config file: ", e);
        }
    }
}
