package net.liyze.liyzetools;

import java.util.ArrayList;

public interface Command {
    void run(ArrayList<String> args);

    String Name();
}
