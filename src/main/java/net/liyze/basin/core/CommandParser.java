package net.liyze.basin.core;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static net.liyze.basin.core.Main.*;

/**
 * Basin Command Parser
 */
@SuppressWarnings("unused")
public class CommandParser {

    /**
     * All Parser.
     */
    public static final List<CommandParser> cs = new ArrayList<>();

    /**
     * This Parser's vars.
     */
    public final Map<String, String> vars = new ConcurrentHashMap<>();

    public CommandParser() {
        cs.add(this);
    }

    /**
     * Sync variables to the public environment.
     */
    public CommandParser sync() {
        vars.putAll(publicVars);
        return this;
    }

    /**
     * Parse command from String.
     */
    public boolean parse(@NotNull String ac) {
        if (ac.isBlank() || ac.startsWith("#")) return true;
        LOGGER.info(ac);
        Splitter sp = Splitter.on(" ").trimResults();
        return parse(Lists.newArrayList(sp.split(ac)));
    }

    /**
     * Parse command from a List.
     */
    public boolean parse(@NotNull List<String> alc) {
        final List<List<String>> allArgs = new ArrayList<>();
        {
            final List<String> areaArgs = new ArrayList<>();
            for (String i : alc) {
                //Multi Command Apply
                if (i.equals("&")) {
                    allArgs.add(areaArgs);
                    areaArgs.clear();
                    continue;
                }
                AtomicReference<String> f = new AtomicReference<>(i);
                AtomicReference<String> s = new AtomicReference<>(null);
                if (s.get() != null) {
                    areaArgs.add(s.get());
                } else {
                    areaArgs.add(i);
                }
            }
            allArgs.add(areaArgs);
        }
        for (List<String> args : allArgs) {
            final String cmdName = args.get(0);
            final Logger LOGGER = LoggerFactory.getLogger(cmdName);
            //Var Define Apply
            if (cmdName.matches(".*=.*")) {
                Splitter sp = Splitter.on("=").trimResults();
                List<String> var = Lists.newArrayList(sp.split(cmdName));
                vars.put(var.get(0).strip(), var.get(1).strip());
                LOGGER.info(cmdName);
                return true;
            }
            args.remove(cmdName);
            final Command run = commands.get(cmdName.toLowerCase().strip());
            //Run command method
            if (!(run == null)) {
                try {
                    LOGGER.debug(cmdName + " started.");
                    run.run(args);
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

    public void parseScript(@NotNull BufferedReader script) throws IOException {
        Stream<String> lines = script.lines();
        lines.forEach(i -> {
            if (!(i).isEmpty()) this.parse(i);
        });
        script.close();
    }
}
