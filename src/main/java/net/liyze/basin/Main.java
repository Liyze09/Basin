package net.liyze.basin;

import java.io.File;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static net.liyze.basin.Commands.regCommands;
import static net.liyze.basin.runCommands.runCommand;
import static net.liyze.basin.util.Out.*;

public class Main {
    public static HashMap<String, Command> commands = new HashMap<>();
    public static boolean debug = false;
    static Thread scanCmd = new Thread(Main::scanConsole);
    public static final ExecutorService taskPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()+1);
    public static final ExecutorService servicePool = Executors.newCachedThreadPool();

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void main(String[] args) {
        File data = new File("data");
        data.mkdirs();
        regCommands();
        scanCmd.start();
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

    public static void stopAll() {
        info("Stopping!");
        scanCmd.interrupt();
        taskPool.shutdown();
        servicePool.shutdownNow();
        System.exit(0);
    }
}
