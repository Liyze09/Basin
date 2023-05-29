package net.liyze.basin.core.commands;

import net.liyze.basin.interfaces.Command;
import net.liyze.basin.core.Main;

import java.util.List;

/**
 * Print all command loaded.
 *
 * @author Liyze09
 */
public class ListCommand implements Command {
    @Override
    public void run(List<String> args) {
        for (String i : Main.commands.keySet()) {
            System.out.println(i);
        }
    }

    @Override
    public String Name() {
        return "list";
    }
}
