package net.liyze.basin.core;

import com.fasterxml.jackson.databind.ObjectMapper;

import static net.liyze.basin.core.Main.config;

public final class Config {
    public static Config cfg = new Config();
    public int taskPoolSize = Runtime.getRuntime().availableProcessors() + 1;

    static void initConfig() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        if (!config.exists()) {
            mapper.writeValue(config, cfg);
        }
        cfg = mapper.readValue(config, Config.class);
    }
}
