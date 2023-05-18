package net.liyze.basin;

import com.moandjiezana.toml.Toml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static net.liyze.basin.Commands.regCommands;
import static net.liyze.basin.Loader.loadFilePlugins;
import static net.liyze.basin.RunCommands.runCommand;
import static net.liyze.basin.util.Out.info;

public class Main {
    public static final Logger LOGGER = LoggerFactory.getLogger("Basin System");
    public static HashMap<String, Command> commands = new HashMap<>();
    public static Toml config = new Toml();
    public static Toml env = new Toml();
    public static boolean debug = false;
    static Thread scanCmd = new Thread(Main::scanConsole);
    static Thread loadPlugins = new Thread(Main::loadPlugins);
    public static final ExecutorService taskPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
    public static final ExecutorService servicePool = Executors.newCachedThreadPool();
    static File plugins = new File("data" + File.separator + "plugins");

    public static void main(String[] args) {
        init();
        regCommands();
        loadPlugins.start();
        System.out.println("Basin " + Basin.getVersion());
        scanCmd.start();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void init(){
        File script = new File("data" + File.separator + "script");
        script.mkdirs();
        plugins.mkdirs();
        config.read(InputStream.class.getResourceAsStream("/config.toml"));
        LOGGER.info("Inited");
    }

    private static void scanConsole() {
        Scanner scanner = new Scanner(System.in);
        String command;
        while (!scanCmd.isInterrupted()) {
            command = scanner.nextLine();
            command = command.toLowerCase().strip();
            runCommand(command);
        }
    }

    private static void loadPlugins() {
        try {
            loadFilePlugins();
        } catch (Exception ignored) {
        }
    }

    public static void stopAll() {
        info("Stopping!");
        scanCmd.interrupt();
        taskPool.shutdown();
        servicePool.shutdownNow();
        System.exit(0);
    }
}
