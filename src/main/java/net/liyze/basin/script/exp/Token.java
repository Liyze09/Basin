package net.liyze.basin.script.exp;

import java.io.Serializable;

public abstract class Token implements Serializable {
    protected String name;

    public final String getName() {
        return name;
    }
}
