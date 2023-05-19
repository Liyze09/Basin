package net.liyze.basin;

import com.moandjiezana.toml.Toml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static net.liyze.basin.Basin.basin;
import static net.liyze.basin.Commands.regCommands;
import static net.liyze.basin.Config.cfg;
import static net.liyze.basin.Loader.loadFilePlugins;
import static net.liyze.basin.RunCommands.runCommand;

public final class Main {
    public static final Logger LOGGER = LoggerFactory.getLogger("Basin System");
    public static HashMap<String, Command> commands = new HashMap<>();
    public static Toml config = new Toml();
    public static Toml env = new Toml();
    public static File userHome = new File("data" + File.separator + "home");
    public static boolean debug = false;
    static Thread scanCmd = new Thread(Main::scanConsole);
    static Thread loadPlugins = new Thread(Main::loadPlugins);
    public static final ExecutorService taskPool = Executors.newFixedThreadPool(cfg.taskPoolSize);
    public static final ExecutorService servicePool = Executors.newCachedThreadPool();
    static File plugins = new File("data" + File.separator + "plugins");
    public static Map<String, Object> envMap;

    public static void main(String[] args) {
        try {
            init();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        regCommands();
        loadPlugins.start();
        System.out.println("Basin " + Basin.getVersion());
        System.out.println(basin);
        scanCmd.start();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void init() {
        userHome.mkdirs();
        plugins.mkdirs();
        File cfgFile = new File("data" + File.separator + "cfg.toml");
        File envFile = new File("data" + File.separator + "env.toml");
        if (!cfgFile.exists()) {
            try {
                cfgFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (!envFile.exists()) {
            try {
                envFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try (Writer writer = new FileWriter(envFile)) {
                writer.append("# Basin Environment Variable");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        config.read(cfgFile).to(Config.class);
        envMap = env.read(envFile).toMap();
        LOGGER.info("Inited");
    }

    private static String command;
    private static void scanConsole() {
        Scanner scanner = new Scanner(System.in);
        while (!scanCmd.isInterrupted()) {
            command = scanner.nextLine();
            command = command.toLowerCase().strip();
            taskPool.submit(new Task());
        }
    }

    private static void loadPlugins() {
        try {
            loadFilePlugins();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static class Task implements Runnable {
        @Override
        public void run() {
            try {
                runCommand(command);
            } catch (RuntimeException e) {
                try {
                    e.printStackTrace();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }
}
