package net.liyze.basin.core;

import bscript.BScriptClassLoader;
import bscript.BScriptEvent;
import bscript.BScriptHelper;
import bscript.BScriptRuntime;
import com.google.common.base.Splitter;
import com.itranswarp.summer.AnnotationConfigApplicationContext;
import com.itranswarp.summer.ApplicationContext;
import com.itranswarp.summer.annotation.ComponentScan;
import com.moandjiezana.toml.Toml;
import net.liyze.basin.remote.RemoteServer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarFile;

import static net.liyze.basin.core.CommandParser.cs;
import static net.liyze.basin.core.scan.ServerCommand.serverMap;
import static net.liyze.basin.remote.RemoteServer.servers;

/**
 * Basin start class
 */
@ComponentScan(value = {"net.liyze.basin.core.scan"})
public final class Basin {
    public static final Logger LOGGER = LoggerFactory.getLogger("Basin");
    public static final HashMap<String, Command> commands = new HashMap<>();
    public static final File userHome = new File("data" + File.separator + "home");
    public final static File config = new File("data" + File.separator + "cfg.json");
    public static final File script = new File("data" + File.separator + "script");
    public final static List<Class<?>> BootClasses = new ArrayList<>();
    public static final CommandParser CONSOLE_COMMAND_PARSER = new CommandParser();
    public static final Map<String, String> publicVars = new ConcurrentHashMap<>();
    public static final List<ApplicationContext> contexts = new ArrayList<>();
    static final File jars = new File("data" + File.separator + "jars");
    public static Toml env = new Toml();
    public static ExecutorService servicePool = Executors.newCachedThreadPool();
    public static ExecutorService taskPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
    public static Map<String, Object> envMap = new HashMap<>();
    public static Config cfg = Config.initConfig();
    public static ApplicationContext app;
    public static BScriptRuntime runtime = new BScriptRuntime();
    private static String command;
    /**
     * Basin's ASCII banner
     */
    @SuppressWarnings("SpellCheckingInspection")
    public static String banner = String.format(
            """
                    \r
                    BBBBBBBBBBBBBBBBB                                         iiii
                    B::::::::::::::::B                                       i::::i
                    B::::::BBBBBB:::::B                                       iiii
                    BB:::B      B:::::B
                    B::::B       B:::::B   aaaaaaaaaaaaa        ssssssssss   iiiiiii   nnnn  nnnnnnnn
                    B::::B      B:::::B   a::::::::::::a     ss::::::::::s   i:::::i  n:::nn::::::::nn
                    B::::BBBBBB:::::B     aaaaaaaaa:::::a  ss:::::::::::::s   i::::i  n::::::::::::::nn
                    B::::::::::::BB                a::::a  s::::::ssss:::::s  i::::i  nn::::::::::::::n
                    B::::BBBBBB:::::B       aaaaaaa:::::a   s:::::s   ssssss  i::::i   n:::::nnnn:::::n
                    B::::B      B:::::B    aa::::::::::::a     s::::::s       i::::i   n::::n    n::::n
                    B::::B       B:::::B  a::::aaaa::::::a        s::::::s    i::::i   n::::n    n::::n
                    B::::B      B:::::B  a::::a    a:::::a  ssssss   s:::::s  i::::i   n::::n    n::::n
                    BB:::::BBBBBB::::::B a::::a    a:::::a s:::::ssss::::::s i::::::i  n::::n    n::::n
                    B:::::::::::::::::B a:::::aaaa::::::a  s::::::::::::::s  i::::::i  n::::n    n::::n
                    B::::::::::::::::B   a::::::::::aa:::a  s:::::::::::ss   i::::::i  n::::n    n::::n
                    BBBBBBBBBBBBBBBBB     aaaaaaaaaa  aaaa   sssssssssss     iiiiiiii  nnnnnn    nnnnnn
                    :: Basin :: (%s)
                    """, getVersion());

    private Basin() {
        throw new UnsupportedOperationException();
    }

    public static void main(String @NotNull [] args) {
        if (args.length > 0)
            switch (args[0]) {
                case "-compile" -> BScriptHelper.getInstance().compileFiles(new File(args[1]));
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
                script.mkdirs();
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
                    app.findBeanDefinitions(Command.class).forEach(def -> registerCommand((Command) def.getInstance()));

                    if (!cfg.startCommand.isBlank()) CONSOLE_COMMAND_PARSER.parse(cfg.startCommand);
                    if (cfg.enableRemote && !cfg.accessToken.isBlank()) {
                        try {
                            new RemoteServer(cfg.accessToken, cfg.remotePort, new CommandParser()).start();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                loadScripts();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
        try {
            new Thread(() -> {
                try (Scanner scanner = new Scanner(System.in)) {
                    while (true) {
                        command = scanner.nextLine();
                        runtime.broadcast("executeCommand", new BScriptEvent("executeCommand", command));
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
        System.out.println(banner);
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

    public static void loadScripts() {
        BScriptHelper.getInstance().compileFiles(Objects.requireNonNull(script.listFiles((dir, name) -> name.endsWith(".bs"))));
        Map<String, byte[]> bytes = new HashMap<>();
        Arrays.stream(Objects.requireNonNull(script
                        .listFiles((dir, name) -> name.endsWith(".class"))))
                .toList().forEach(clazz -> {
                    try (InputStream input = new FileInputStream(clazz)) {
                        bytes.put("bscript.classes." + clazz.getName().substring(0, clazz.getName().length() - 6),
                                input.readAllBytes());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
        try (URLClassLoader loader = new BScriptClassLoader(bytes)) {
            bytes.keySet().forEach(name -> {
                try {
                    runtime.load(loader.loadClass(name));
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        runtime.run();
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
                                registerCommand((Command) command);
                            } else {
                                LOGGER.warn("Command-Class {} is unsupported", jar.getName());
                            }
                        }
                    }
                }
            }
        }
    }

    public static void registerCommand(Command cmd) {
        commands.put(cmd.Name(), cmd);
    }

    /**
     * Get version version's String.
     */
    @Contract(pure = true)
    public static @NotNull String getVersion() {
        return "1.6";
    }

    /**
     * Get version version's int.
     */
    public static int getVersionNum() {
        return 6;
    }

    /**
     * Stop basin after all task finished.
     */
    public static void shutdown() {
        new Thread(() -> {
            Basin.LOGGER.info("Stopping\n");
            runtime.broadcast("shuttingDown");
            BootClasses.forEach((i) -> {
                try {
                    ((BasinBoot) i.getDeclaredConstructor().newInstance()).beforeStop();
                } catch (Exception ignored) {
                }
            });
            runtime.broadcast("shutdown");
            taskPool.shutdown();
            runtime.pool.shutdown();
            servicePool.shutdownNow();
            try {
                runtime.pool.awaitTermination(1, TimeUnit.SECONDS);
                taskPool.awaitTermination(4, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                System.exit(0);
            }
            System.exit(0);
        }).start();
    }

    /**
     * Restart basin.
     */
    public static void restart() {
        new Thread(() -> {
            runtime.broadcast("restarting");
            BootClasses.forEach((i) -> {
                try {
                    BasinBoot in = (BasinBoot) i.getDeclaredConstructor().newInstance();
                    in.beforeStop();
                } catch (Exception e) {
                    LOGGER.error(e.toString());
                }
            });
            cs.forEach(c -> c.vars.clear());
            taskPool.shutdownNow();
            servicePool.shutdownNow();
            servers.forEach(Server::stop);
            servers.clear();
            serverMap.values().forEach(Server::stop);
            serverMap.clear();
            commands.clear();
            BootClasses.clear();
            publicVars.clear();
            try {
                taskPool.awaitTermination(3, TimeUnit.SECONDS);
            } catch (InterruptedException ignore) {
            }
            taskPool = Executors.newFixedThreadPool(cfg.taskPoolSize);
            servicePool = Executors.newCachedThreadPool();
            app.close();
            app = new AnnotationConfigApplicationContext(Basin.class);
            app.findBeanDefinitions(Command.class).forEach(def -> registerCommand((Command) def.getInstance()));
            try {
                loadEnv();
            } catch (Exception e) {
                LOGGER.error(e.toString());
            }
            try {
                loadJars();
                BootClasses.forEach((i) -> {
                    try {
                        BasinBoot in = (BasinBoot) i.getDeclaredConstructor().newInstance();
                        in.afterStart();
                    } catch (Exception e) {
                        LOGGER.error(e.toString());
                    }
                });
            } catch (Exception e) {
                LOGGER.error(e.toString());
            }
            if (!cfg.startCommand.isBlank()) CONSOLE_COMMAND_PARSER.parse(cfg.startCommand);
            if (cfg.enableRemote && !cfg.accessToken.isBlank()) {
                try {
                    new RemoteServer(cfg.accessToken, cfg.remotePort, new CommandParser()).start();
                } catch (Exception e) {
                    LOGGER.error(e.toString());
                }
            }
            runtime.broadcast("restarted");
            LOGGER.info("Restarted!");
        }).start();
    }
}
