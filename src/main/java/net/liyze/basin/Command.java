package net.liyze.basin;

import java.util.ArrayList;

public interface Command {
    /**
     * What to do when start the command
     *
     * @author Liyze09
     */
    void run(ArrayList<String> args);

    /**
     * The Name of the command
     */

    String Name();
}
