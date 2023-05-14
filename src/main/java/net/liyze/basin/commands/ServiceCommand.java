package net.liyze.basin.commands;

import net.liyze.basin.Command;
import net.liyze.basin.util.Out;

import java.util.ArrayList;

import static net.liyze.basin.Main.*;
import static net.liyze.basin.RunCommands.runCommand;

public class ServiceCommand implements Command {

    static String cmd= "";
    @Override
    public void run(ArrayList<String> args) {
        cmd=String.join(" ",args);
        servicePool.submit(new Service());
    }

    @Override
    public String Name() {
        return "service";
    }
}
class Service implements Runnable{
    @Override
    public void run() {
        Out.info("start: "+ExecuteCommand.cmd);
        runCommand(ExecuteCommand.cmd);
    }
}
