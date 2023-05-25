package net.liyze.basin.core;

import net.liyze.basin.api.BasinBoot;
import net.liyze.basin.api.Command;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

import static net.liyze.basin.core.Util.register;


public abstract class Loader {
    public static List<Class<?>> BootClasses = new ArrayList<>();

    public static void loadJars() throws Exception {
        File[] children = Main.jars.listFiles((file, s) -> s.matches(".*\\.jar"));
        String b, c;
        String[] bl, cl;
        if (children == null) {
            Main.LOGGER.error("Plugin file isn't exist!");
        } else {
            for (File jar : children) {
                try (JarFile jarFile = new JarFile(jar)) {
                    b = jarFile.getManifest().getMainAttributes().getValue("Boot-Class");
                    bl = b.split("[ ,;]");
                    c = jarFile.getManifest().getMainAttributes().getValue("Export-Command");
                    cl = c.split("[ ,;]");
                    for (String i : bl) {
                        if (!b.isBlank()) {
                            Class<?> cls = Class.forName(i);
                            Object boot = cls.getDeclaredConstructor().newInstance();
                            if (boot instanceof BasinBoot) {
                                ((BasinBoot) boot).onStart();
                                BootClasses.add(cls);
                            } else {
                                Main.LOGGER.warn("App {} is unsupported", jar.getName());
                            }
                        }
                    }
                    for (String i : cl) {
                        if (!c.isBlank()) {
                            Class<?> cls = Class.forName(i);
                            Object command = cls.getDeclaredConstructor().newInstance();
                            if (command instanceof Command) {
                                register((Command) command);
                            } else {
                                Main.LOGGER.warn("Plugin {} is unsupported", jar.getName());
                            }
                        }
                    }
                }
            }
        }
    }
}