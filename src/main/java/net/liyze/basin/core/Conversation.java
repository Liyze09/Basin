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

/**
 * Basin Command Parser
 */
public class Conversation {
    public static final List<Conversation> cs = new ArrayList<>();
    public final Map<String, String> vars = new ConcurrentHashMap<>();

    public Conversation() {
        cs.add(this);
    }

    public Conversation sync() {
        vars.putAll(publicVars);
        return this;
    }

    /**
     * Parse command from String
     */
    public boolean parse(@NotNull String ac) {
        if (ac.isBlank() || ac.startsWith("#")) return true;
        List<String> alc = new ArrayList<>(List.of(StringUtils.split(ac.strip().replace("/", ""), ' ')));
        return parse(alc);
    }

    public boolean parse(@NotNull List<String> alc) {
        final List<List<String>> allArgs = new ArrayList<>();
        {
            List<String> areaArgs = new ArrayList<>();
            //Pre-parse
            for (String i : alc) {
                //Multi Command
                if (i.equals("&")) {
                    allArgs.add(areaArgs);
                    areaArgs.clear();
                    continue;
                }
                //Var Apply
                if (!i.startsWith("$")) {
                    areaArgs.add(i);
                } else {
                    i = i.replaceFirst("\\$", "");
                    String string = vars.get(i);
                    if (string != null) {
                        areaArgs.add(string);
                    } else {
                        String string0 = vars.get(i);
                        if (string0 != null) {
                            areaArgs.add(string0);
                        } else {
                            LOGGER.warn("Undefined Variable {}", i);
                        }
                    }
                }
            }
            allArgs.add(areaArgs);
        }
        for (List<String> args : allArgs) {
            String cmdName = args.get(0);
            final Logger LOGGER = LoggerFactory.getLogger(cmdName);
            //Var Define Apply
            if (cmdName.matches(".*=.*")) {
                String[] var = StringUtils.split(cmdName, "=");
                vars.put(var[0].strip(), var[1].strip());
                LOGGER.info(cmdName);
                return true;
            }
            args.remove(cmdName);
            Command run = commands.get(cmdName.toLowerCase().strip());
            //Run command method
            if (!(run == null)) {
                try {
                    LOGGER.debug(cmdName + "started.");
                    if (cfg.enableParallel) {
                        run.run(args);
                    } else {
                        synchronized (this) {
                            run.run(args);
                        }
                    }
                    return true;
                } catch (IndexOutOfBoundsException e) {
                    LOGGER.error("Bad arg input.");
                } catch (Exception e) {
                    LOGGER.error(e.toString());
                }
            } else LOGGER.error("Unknown command: " + cmdName);
        }
        return false;
    }

}
