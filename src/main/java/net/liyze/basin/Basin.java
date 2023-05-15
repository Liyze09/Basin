package net.liyze.basin;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class Basin {
    @Contract(pure = true)
    public static @NotNull String getVersion(){
        return "0.1";
    }
    public static float getVersionNum(){
        return 0.1F;
    }
}
