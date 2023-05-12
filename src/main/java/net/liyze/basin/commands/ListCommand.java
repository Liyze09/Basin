package net.liyze.basin.commands;

import net.liyze.basin.Command;

import java.util.ArrayList;

import static net.liyze.basin.Main.commands;

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
