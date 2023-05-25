package net.liyze.basin.core;

import com.moandjiezana.toml.Toml;
import net.liyze.basin.api.BasinBoot;
import net.liyze.basin.api.Command;
import net.liyze.basin.core.commands.ScriptCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static net.liyze.basin.core.Basin.basin;
import static net.liyze.basin.core.Commands.regCommands;
import static net.liyze.basin.core.Loader.*;
import static net.liyze.basin.core.RunCommands.runCommand;

public final class Main {
    public static final Logger LOGGER = LoggerFactory.getLogger("Basin");
    public static final HashMap<String, Command> commands = new HashMap<>();
    public static File config = new File("data" + File.separator + "cfg.json");
    public static final Toml env = new Toml();
    public static final File userHome = new File("data" + File.separator + "home");
    public static boolean debug = false;
    public static ExecutorService taskPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
    public static final ExecutorService servicePool = Executors.newCachedThreadPool();
    static final File jars = new File("data" + File.separator + "jars");
    public static Map<String, Object> envMap;
    private static String command;

    public static void main(String[] args) {
        LOGGER.info("Basin started.");
        Thread init = new Thread(() -> {
            try {
                init();
                LOGGER.info("Init method are finished.");
                loadJars();
                LOGGER.info("Loader's method are finished.");
                BootClasses.forEach((i) -> new Thread(() -> {
                    try {
                        ((BasinBoot) i.getDeclaredConstructor().newInstance()).afterStart();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }));
                LOGGER.info("Startup method are finished.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        taskPool.submit(init);
        new Thread(() -> {
            regCommands();
            Scanner scanner = new Scanner(System.in);
            while (true) {
                command = scanner.nextLine();
                taskPool.submit(new Task());
            }
        }).start();
        System.out.println("Basin " + Basin.getVersion());
        System.out.println(basin);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void init() {
        userHome.mkdirs();
        jars.mkdirs();
        File envFile = new File("data" + File.separator + "env.toml");
        try {
            Config.initConfig();
        } catch (Exception e) {
            LOGGER.error("Error when load config file: ", e);
        }
        if (!envFile.exists()) {
            try {
                envFile.createNewFile();
            } catch (IOException e) {
                LOGGER.error("Error when create environment variable file: ", e);
            }
            try (Writer writer = new FileWriter(envFile)) {
                writer.append("# Basin Environment Variable");
            } catch (IOException e) {
                LOGGER.error("Error when create environment variable file: ", e);
            }
        }
        envMap = env.read(envFile).toMap();
        try {
            ArrayList<String> i = new ArrayList<>();
            i.add("BOOT");
            new ScriptCommand().run(i);
        } catch (RuntimeException ignored) {
        }
        taskPool = Executors.newFixedThreadPool(Config.cfg.taskPoolSize);
        debug = Config.cfg.debug;
    }

    static class Task implements Runnable {
        @Override
        public void run() {
            try {
                runCommand(command);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
