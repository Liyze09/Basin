package net.liyze.basin.core;

import com.moandjiezana.toml.Toml;
import net.liyze.basin.context.AnnotationConfigApplicationContext;
import net.liyze.basin.context.ConfigurableApplicationContext;
import net.liyze.basin.remote.RemoteServer;
import net.liyze.basin.script.Parser;
import net.liyze.basin.script.AbstractPreParser;
import org.apache.commons.lang3.StringUtils;
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

import static net.liyze.basin.script.Parser.ps;

/**
 * Basin start class
 */
public final class Main {
    public static final Logger LOGGER = LoggerFactory.getLogger("Basin");
    public static final HashMap<String, Command> commands = new HashMap<>();
    public static final File userHome = new File("data" + File.separator + "home");
    public final static File config = new File("data" + File.separator + "cfg.json");
    public final static List<Class<?>> BootClasses = new ArrayList<>();
    public static final Parser CONSOLE_PARSER = new Parser();
    public static final Map<String, String> publicVars = new ConcurrentHashMap<>();
    public static final List<AnnotationConfigApplicationContext> contexts = new ArrayList<>();
    static final File jars = new File("data" + File.separator + "jars");
    public static Toml env = new Toml();
    public static ExecutorService servicePool = Executors.newCachedThreadPool();
    public static ExecutorService taskPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
    public static Map<String, Object> envMap;
    public static Config cfg = Config.initConfig();
    private static String command;
    public static ConfigurableApplicationContext app;
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        LOGGER.info("----------------------------------------------\nBasin started.");
        taskPool.submit(new Thread(() -> {
            try {
                init();
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
                            LOGGER.error(e.toString());
                        }
                    })));
                    LOGGER.info("Startup method are finished.");
                }
            } catch (Exception e) {
                LOGGER.error(e.toString());
            }
        }));
        new Thread(() -> {
            app = new AnnotationConfigApplicationContext(Basin.class);
            app.findBeanDefinitions(Command.class).forEach(def -> register((Command) def.getInstance()));
            app.findBeanDefinitions(AbstractPreParser.class).forEach(def -> ps.add((Class<AbstractPreParser>) def.getBeanClass()));
            Parser parser = new Parser();
            regCommands();
            if (cfg.enableRemote && !cfg.accessToken.isBlank()) {
                try {
                    new RemoteServer(cfg.accessToken, cfg.remotePort, new Parser()).start();
                } catch (Exception e) {
                    LOGGER.error(e.toString());
                }
            }
            if (!cfg.startCommand.isBlank()) CONSOLE_PARSER.parse(cfg.startCommand);
            try (Scanner scanner = new Scanner(System.in)) {
                while (true) {
                    command = scanner.nextLine();
                    if (cfg.enableParallel) {
                        taskPool.submit(new Thread(() -> {
                            try {
                                parser.sync().parse(command);
                            } catch (Exception e) {
                                LOGGER.error(e.toString());
                            }
                        }));
                    } else {
                        try {
                            parser.sync().parse(command);
                        } catch (Exception e) {
                            LOGGER.error(e.toString());
                        }
                    }
                }
            }
        }).start();
        System.out.println(Basin.getBasin().banner);
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    public static void init() throws IOException {
        userHome.mkdirs();
        jars.mkdirs();
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
        envMap = env.read(envFile).toMap();
        taskPool = Executors.newFixedThreadPool(cfg.taskPoolSize);
    }

    public static void loadJars() throws Exception {
        File[] children = jars.listFiles((file, s) -> s.matches(".*\\.jar"));
        String b, c;
        String[] bl, cl;
        if (children == null) {
            LOGGER.error("Jars file isn't exist!");
        } else {
            for (File jar : children) {
                try (JarFile jarFile = new JarFile(jar)) {
                    b = jarFile.getManifest().getMainAttributes().getValue("Boot-Class");
                    bl = StringUtils.split(b, ' ');
                    c = jarFile.getManifest().getMainAttributes().getValue("Export-Command");
                    cl = StringUtils.split(c, ' ');
                    for (String i : bl) {
                        if (!b.isBlank()) {
                            Class<?> cls = Class.forName(i);
                            Object boot = cls.getDeclaredConstructor().newInstance();
                            if (boot instanceof BasinBoot) {
                                new Thread(() -> ((BasinBoot) boot).onStart()).start();
                                BootClasses.add(cls);
                            } else {
                                LOGGER.warn("App {} is unsupported", jar.getName());
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
                                LOGGER.warn("Plugin {} is unsupported", jar.getName());
                            }
                        }
                    }
                }
            }
        }
    }

    public static void regCommands() {/*
        register(new ForceStopCommand());
        register(new StopCommand());
        register(new EquationCommand());
        register(new ListCommand());
        register(new BenchCommand());
        register(new ExecuteCommand());
        register(new ScriptCommand());
        register(new ServerCommand());
        register(new RestartCommand());
        register(new PublicCommand());
        register(new RemoteCommand());
        if (cfg.enableShellCommand) register(new ShellCommand());*/
    }

    public static void register(Command cmd) {
        commands.put(cmd.Name(), cmd);
    }
}
