package net.liyze.basin.commands;

import net.liyze.basin.Command;
import net.liyze.basin.util.Out;

import java.util.ArrayList;

import static net.liyze.basin.Main.taskPool;
import static net.liyze.basin.RunCommands.runCommand;

public class ExecuteCommand implements Command {
    static String cmd = "";

    @Override
    public void run(ArrayList<String> args) {
        cmd = String.join(" ", args);
        taskPool.submit(new Task());
    }

    @Override
    public String Name() {
        return "execute";
    }
}

class Task implements Runnable {
    @Override
    public void run() {
        Out.info("execute: " + ExecuteCommand.cmd);
        runCommand(ExecuteCommand.cmd);
    }
}

