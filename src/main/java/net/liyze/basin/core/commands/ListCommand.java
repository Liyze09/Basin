package net.liyze.basin.core.commands;

import net.liyze.basin.api.Command;
import net.liyze.basin.core.Main;

import java.util.ArrayList;

/**
 * Print all command loaded.
 * @author Liyze09
 */
public class ListCommand implements Command {
    @Override
    public void run(ArrayList<String> args) {
        for (String i : Main.commands.keySet()) {
            System.out.println(i);
        }
    }

    @Override
    public String Name() {
        return "list";
    }
}
