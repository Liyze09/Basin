package net.liyze.basin.core;

import net.liyze.basin.interfaces.Command;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static net.liyze.basin.core.Main.*;

public class Conversation {
    public final Map<String, String> vars = new ConcurrentHashMap<>();

    @SuppressWarnings("DataFlowIssue")
    public boolean parse(@NotNull String ac) {
        if (ac.isBlank() || ac.startsWith("#")) return true;
        ArrayList<String> alc = new ArrayList<>(List.of(StringUtils.split(ac.strip().replace("/", ""), '&')));
        ArrayList<String> args = new ArrayList<>();
        for (String cmd : alc) {
            for (String i : List.of(StringUtils.split(cmd.strip(), ' '))) {
                if (!i.startsWith("$")) {
                    args.add(i);
                } else {
                    i = i.replaceFirst("\\$", "");
                    String string = vars.get(i);
                    if (string != null) {
                        args.add(string);
                    } else {
                        String string0 = vars.get(i);
                        if (string0 != null) {
                            args.add(string0);
                        } else {
                            LOGGER.info("Undefined Variable {}", i);
                        }
                    }
                }
            }
            String cmdName = args.get(0);
            final Logger LOGGER = LoggerFactory.getLogger(cmdName);
            if (cmdName.matches(".*=.*")) {
                String[] var = StringUtils.split(cmdName, "=");
                vars.put(var[0].strip(), var[1].strip());
                return true;
            }
            args.remove(cmdName);
            Command run = commands.get(cmdName.toLowerCase().strip());
            LOGGER.info("Starting: " + cmd);
            if (!(run == null)) {
                try {
                    run.run(args);
                    return true;
                } catch (IndexOutOfBoundsException e) {
                    LOGGER.error("Bad arg input.");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else LOGGER.error("Unknown command: " + cmdName);
        }
        return false;
    }
}
