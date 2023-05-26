package net.liyze.basin.api;

import java.util.List;

public interface Command {
    /**
     * What to do when start the command
     *
     * @author Liyze09
     */
    void run(List<String> args);

    /**
     * The Name of the command
     */

    String Name();
}
