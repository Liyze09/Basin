package net.liyze.liyzetools;

import java.util.HashMap;
import java.util.Scanner;

import static net.liyze.liyzetools.Commands.regCommands;
import static net.liyze.liyzetools.runCommands.runCommand;
import static net.liyze.liyzetools.util.Out.info;

public class Main {
    public static HashMap<String, Command> commands = new HashMap<>();
    public static boolean debug = false;
    static Thread scanCmd = new Thread(Main::scanConsole);

    public static void main(String[] args) {
        regCommands();
        scanCmd.start();
    }

    private static void scanConsole() {
        Scanner scanner = new Scanner(System.in);
        String command;
        while (!scanCmd.isInterrupted()) {
            System.out.print("/");
            command = scanner.nextLine();
            command = command.toLowerCase().strip();
            runCommand(command);
        }
    }

    public static void stopAll() {
        info("Stopping!");
        scanCmd.interrupt();
    }
}
