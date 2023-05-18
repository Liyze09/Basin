package net.liyze.basin;

import java.io.File;
import java.util.jar.JarFile;

import static net.liyze.basin.Main.*;
import static net.liyze.basin.util.Register.register;


public abstract class Loader {

    public static void loadFilePlugins() throws Exception {
        File[] children = plugins.listFiles((file, s) -> s.matches(".*\\.jar"));
        String c = "";
        if (children == null) {
            LOGGER.error("Plugin file isn't exists!");
        } else {
            for (File jar : children) {
                try (JarFile jarFile = new JarFile(jar)) {
                    c = jarFile.getManifest().getMainAttributes().getValue("Export-Command");
                    if (!c.isBlank()) {
                        Class<?> cls = Class.forName(c);
                        register((Command) cls.getDeclaredConstructor().newInstance());
                    }
                }
            }
        }
    }
}