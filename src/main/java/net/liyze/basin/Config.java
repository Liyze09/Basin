package net.liyze.basin;

public class Config {
    public static Config cfg = new Config();

    private Config() {
    }

    public int taskPoolSize = Runtime.getRuntime().availableProcessors() + 1;
}
