package net.liyze.basin.core.commands;

import net.liyze.basin.interfaces.Command;
import net.liyze.basin.core.Main;

import java.util.List;

import static net.liyze.basin.core.Main.runCommand;

/**
 * Put command into a CachedThreadPool
 * /execute [command] [args..]
 *
 * @author Liyze09
 */
public class ExecuteCommand implements Command {

    static String cmd = "";

    @Override
    public void run(List<String> args) {
        cmd = String.join(" ", args);
        Main.servicePool.submit(new Service());
    }

    @Override
    public String Name() {
        return "execute";
    }

    static class Service implements Runnable {
        @Override
        public void run() {
            Main.LOGGER.info("start: " + ExecuteCommand.cmd);
            runCommand(ExecuteCommand.cmd);
        }
    }
}


