package net.liyze.basin.core;

import java.io.File;
import java.util.jar.JarFile;

import static net.liyze.basin.core.Util.register;


public abstract class Loader {
    public static void loadFilePlugins() throws Exception {
        File[] children = Main.plugins.listFiles((file, s) -> s.matches(".*\\.jar"));
        String c;
        if (children == null) {
            Main.LOGGER.error("Plugin file isn't exist!");
        } else {
            for (File jar : children) {
                try (JarFile jarFile = new JarFile(jar)) {
                    c = jarFile.getManifest().getMainAttributes().getValue("Export-Command");
                    if (!c.isBlank()) {
                        Class<?> cls = Class.forName(c);
                        Object command = cls.getDeclaredConstructor().newInstance();
                        if (command instanceof Command) {
                            register((Command) command);
                        } else {
                            Main.LOGGER.warn("Plugin {} is unsupport", jar.getName());
                        }
                    }
                }
            }
        }
    }
}