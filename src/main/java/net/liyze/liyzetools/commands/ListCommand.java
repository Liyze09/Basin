package net.liyze.liyzetools.commands;

import net.liyze.liyzetools.Command;

import java.util.ArrayList;

import static net.liyze.liyzetools.Main.commands;

public class ListCommand implements Command {
    @Override
    public void run(ArrayList<String> args) {
        for (String i : commands.keySet()) {
            System.out.println(i);
        }
    }

    @Override
    public String Name() {
        return "list";
    }
}
