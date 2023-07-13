package net.liyze.basin.core;

import bscript.BScriptHelper;
import com.google.common.base.Splitter;
import com.itranswarp.summer.context.AnnotationConfigApplicationContext;
import com.itranswarp.summer.context.ApplicationContext;
import com.moandjiezana.toml.Toml;
import net.liyze.basin.mixin.MixinProcessor;
import net.liyze.basin.remote.RemoteServer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.jar.JarFile;

/**
 * Basin start class
 */
public final class Main {
    public static final Logger LOGGER = LoggerFactory.getLogger("Basin");
    public static final HashMap<String, Command> commands = new HashMap<>();
    public static final File userHome = new File("data" + File.separator + "home");
    public final static File config = new File("data" + File.separator + "cfg.json");
    public final static List<Class<?>> BootClasses = new ArrayList<>();
    public static final CommandParser CONSOLE_COMMAND_PARSER = new CommandParser();
    public static final Map<String, String> publicVars = new ConcurrentHashMap<>();
    public static final List<ApplicationContext> contexts = new ArrayList<ApplicationContext>();
    static final File jars = new File("data" + File.separator + "jars");
    public static Toml env = new Toml();
    public static ExecutorService servicePool = Executors.newCachedThreadPool();
    public static ExecutorService taskPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
    public static Map<String, Object> envMap = new HashMap<>();
    public static Config cfg = Config.initConfig();
    public static ApplicationContext app;
    private static String command;


    public static void main(String @NotNull [] args) throws IOException {
        if (args.length > 0)
            switch (args[0]) {
                case "-compile" -> BScriptHelper.getInstance().compileFile(new File(args[1]));
                case "-execute" -> BScriptHelper.getInstance().executeFile(new File(args[1]));
                case "-interpret" -> BScriptHelper.getInstance().interpretFile(new File(args[1]));
                default -> LOGGER.warn("Bad arg input.");
            }
        else {
            start();
        }
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    public static void start() {
        LOGGER.info("----------------------------------------------\nBasin started.");
        taskPool.submit(new Thread(() -> {
            try {
                userHome.mkdirs();
                jars.mkdirs();
                loadEnv();
                envMap.forEach((key, value) -> publicVars.put(key, value.toString()));
                LOGGER.info("Init method are finished.");
                if (cfg.doLoadJars) {
                    loadJars();
                    LOGGER.info("Loader's method are finished.");
                    BootClasses.forEach((i) -> servicePool.submit(new Thread(() -> {
                        try {
                            BasinBoot in = (BasinBoot) i.getDeclaredConstructor().newInstance();
                            in.afterStart();
                        } catch (Exception e) {
                            LOGGER.error(e.getMessage());
                        }
                    })));
                    LOGGER.info("Startup method are finished.");
                    app = new AnnotationConfigApplicationContext(Basin.class);
                    app.findBeanDefinitions(Command.class).forEach(def -> register((Command) def.getInstance()));

                    if (!cfg.startCommand.isBlank()) CONSOLE_COMMAND_PARSER.parse(cfg.startCommand);
                    if (cfg.enableRemote && !cfg.accessToken.isBlank()) {
                        try {
                            new RemoteServer(cfg.accessToken, cfg.remotePort, new CommandParser()).start();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                new MixinProcessor(contexts).run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
        try {
            new Thread(() -> {
                try (Scanner scanner = new Scanner(System.in)) {
                    while (true) {
                        command = scanner.nextLine();
                        if (cfg.enableParallel) {
                            taskPool.submit(new Thread(() -> {
                                try {
                                    CONSOLE_COMMAND_PARSER.sync().parse(command);
                                } catch (Exception e) {
                                    LOGGER.error(e.toString());
                                }
                            }));
                        } else {
                            try {
                                CONSOLE_COMMAND_PARSER.sync().parse(command);
                            } catch (Exception e) {
                                LOGGER.error(e.toString());
                            }
                        }
                    }
                }
            }).start();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        System.out.println(Basin.getBasin().banner);
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    public static void loadEnv() throws IOException {
        File envFile = new File("data" + File.separator + "env.toml");
        if (!envFile.exists()) {
            try {
                envFile.createNewFile();
            } catch (IOException e) {
                LOGGER.error("Error when create environment variable file: ", e);
            }
            try (Writer writer = new FileWriter(envFile)) {
                writer.write("# Basin Environment Variables");
            }
        }
        envMap.putAll(env.read(envFile).toMap());
        taskPool = Executors.newFixedThreadPool(cfg.taskPoolSize);
    }

    public static void loadJars() throws Exception {
        File[] children = jars.listFiles((file, s) -> s.matches(".*\\.jar"));
        String b, c;
        Iterable<String> bl, cl;
        if (children == null) {
            LOGGER.error("Jars file isn't exist!");
        } else {
            for (File jar : children) {
                try (JarFile jarFile = new JarFile(jar)) {
                    b = jarFile.getManifest().getMainAttributes().getValue("Boot-Class");
                    Splitter sp = Splitter.on(" ").trimResults();
                    bl = sp.split(b);
                    c = jarFile.getManifest().getMainAttributes().getValue("Export-Command");
                    cl = sp.split(c);
                    for (String i : bl) {
                        if (!b.isBlank()) {
                            Class<?> cls = Class.forName(i);
                            Object boot = cls.getDeclaredConstructor().newInstance();
                            if (boot instanceof BasinBoot) {
                                new Thread(() -> ((BasinBoot) boot).onStart()).start();
                                BootClasses.add(cls);
                            } else {
                                LOGGER.warn("App-BootClass {} is unsupported", jar.getName());
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
                                LOGGER.warn("Command-Class {} is unsupported", jar.getName());
                            }
                        }
                    }
                }
            }
        }
    }

    public static void register(Command cmd) {
        commands.put(cmd.Name(), cmd);
    }
}
