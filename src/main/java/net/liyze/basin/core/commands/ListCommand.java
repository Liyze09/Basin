package net.liyze.basin.core.commands;

import net.liyze.basin.core.Main;
import net.liyze.basin.interfaces.Command;

import java.util.List;
import java.util.Map;

import static net.liyze.basin.core.Main.LOGGER;

/**
 * Print all command loaded.
 *
 * @author Liyze09
 */
public class ListCommand implements Command {
    @Override
    public void run(List<String> args) {
        LOGGER.info("Commands");
        for (String i : Main.commands.keySet()) {
            System.out.println(i);
        }
        LOGGER.info("Var");
        for (Map.Entry<String, String> i : Main.publicVars.entrySet()) {
            System.out.print(i.getKey() + " = ");
            System.out.println(i.getValue());
        }
        LOGGER.info("Env");
        for (Map.Entry<String, Object> i : Main.envMap.entrySet()) {
            System.out.print(i.getKey() + " = ");
            System.out.println(i.getValue());
        }
    }

    @Override
    public String Name() {
        return "list";
    }
}
