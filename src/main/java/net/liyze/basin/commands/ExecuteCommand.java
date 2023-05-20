package net.liyze.basin.commands;

import net.liyze.basin.Command;
import net.liyze.basin.Util;

import java.util.ArrayList;

import static net.liyze.basin.Main.servicePool;
import static net.liyze.basin.RunCommands.runCommand;

/**
 * Put command into a CachedThreadPool
 * /execute [command] [args..]
 *
 * @author Liyze09
 */
public class ExecuteCommand implements Command {

    static String cmd = "";

    @Override
    public void run(ArrayList<String> args) {
        cmd = String.join(" ", args);
        servicePool.submit(new Service());
    }

    @Override
    public String Name() {
        return "execute";
    }

    static class Service implements Runnable {
        @Override
        public void run() {
            Util.info("start: " + ExecuteCommand.cmd);
            runCommand(ExecuteCommand.cmd);
        }
    }
}


