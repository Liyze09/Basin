package net.liyze.basin.core;


import java.util.ArrayList;
import java.util.List;

import static java.lang.System.out;

public abstract class RunCommands {
    public static void runCommand(String cmd) {
        if (!cmd.startsWith("/")) {
            out.println(cmd);
        } else {
            cmd = cmd.toLowerCase().strip().replace("/", "");
            Main.LOGGER.info("Starting: " + cmd);
            String[] array = cmd.split(" ");
            String cmdName = array[0];
            ArrayList<String> args = new ArrayList<>(List.of(array));
            args.remove(cmdName);
            Command run = Main.commands.get(cmdName.strip());
            if (!(run == null)) {
                try {
                    run.run(args);
                } catch (IndexOutOfBoundsException e) {
                    Main.LOGGER.error("Bad arg input.");
                } catch (RuntimeException e) {
                    Main.LOGGER.error(String.valueOf(e));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else Main.LOGGER.error("Unknown command: " + cmdName);
        }
    }
}
