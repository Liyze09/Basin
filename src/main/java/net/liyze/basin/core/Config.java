package net.liyze.basin.core;

import com.google.gson.Gson;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;

import static net.liyze.basin.core.Main.config;

public final class Config {
    public static Config cfg = new Config();
    public int taskPoolSize = Runtime.getRuntime().availableProcessors() + 1;

    static void initConfig() throws Exception {
        Gson gson=new Gson();
        if (!config.exists()){
            try(Writer writer = new FileWriter(config)){
                writer.write(gson.toJson(cfg));
            }
        }
        try(Reader reader = new FileReader(config)){
            gson.fromJson(reader, Config.class);
        }
    }
}
