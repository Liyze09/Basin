package net.liyze.basin.core;

import net.liyze.basin.interfaces.Command;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.liyze.basin.core.Main.commands;

public class Parser {
    public final Map<String, String> vars = new HashMap<>();

    public void parse(@NotNull String ac) {
        if (ac.isBlank()) return;
        ArrayList<String> alc = new ArrayList<>(List.of(StringUtils.split(ac.strip().replace("/", ""), '&')));
        ArrayList<String> args = new ArrayList<>();
        for (String cmd : alc) {
            for (String i : List.of(StringUtils.split(cmd.strip(), ' '))) {
                if (!i.startsWith("$")) {
                    args.add(i);
                } else {
                    String string = vars.get(i.replaceFirst("\\$", ""));
                    if (string != null) {
                        args.add(string);
                    }
                }
            }
            String cmdName = args.get(0);
            final Logger LOGGER = LoggerFactory.getLogger(cmdName);
            if (cmdName.matches(".*=.*")) {
                String[] var = StringUtils.split(cmdName, "=");
                vars.put(var[0].strip(), var[1].strip());
                return;
            }
            args.remove(cmdName);
            Command run = commands.get(cmdName.toLowerCase().strip());
            LOGGER.info("Starting: " + cmd);
            if (!(run == null)) {
                try {
                    run.run(args);
                } catch (IndexOutOfBoundsException e) {
                    LOGGER.error("Bad arg input.");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else LOGGER.error("Unknown command: " + cmdName);
        }
    }
}
