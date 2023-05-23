package net.liyze.basin;


import java.util.ArrayList;
import java.util.List;

import static java.lang.System.out;
import static net.liyze.basin.Main.*;

public abstract class RunCommands {
    public static void runCommand(String cmd) {
        if (!cmd.startsWith("/")) {
            out.println(cmd);
        } else {
            cmd = cmd.toLowerCase().strip().replace("/", "");
            LOGGER.info("Starting: " + cmd);
            String[] array = cmd.split(" ");
            String cmdName = array[0];
            ArrayList<String> args = new ArrayList<>(List.of(array));
            args.remove(cmdName);
            Command run = commands.get(cmdName.strip());
            if (!(run == null)) {
                try {
                    run.run(args);
                } catch (IndexOutOfBoundsException e) {
                    LOGGER.error("Bad arg input.");
                } catch (RuntimeException e) {
                    LOGGER.error(String.valueOf(e));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else LOGGER.error("Unknown command: " + cmdName);
        }
    }
}
