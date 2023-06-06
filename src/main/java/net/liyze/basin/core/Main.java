package net.liyze.basin.core;

import com.moandjiezana.toml.Toml;
import net.liyze.basin.core.commands.*;
import net.liyze.basin.framework.context.AnnotationConfigApplicationContext;
import net.liyze.basin.interfaces.BasinBoot;
import net.liyze.basin.interfaces.Command;
import net.liyze.basin.remote.Server;
import org.apache.commons.lang3.StringUtils;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarFile;


public final class Main {
    public static final Logger LOGGER = LoggerFactory.getLogger("Basin");
    public static final HashMap<String, Command> commands = new HashMap<>();
    public static final File userHome = new File("data" + File.separator + "home");
    public final static File config = new File("data" + File.separator + "cfg.json");
    public final static List<Class<?>> BootClasses = new ArrayList<>();
    public static final Conversation CONSOLE_CONVERSATION = new Conversation();
    static final File jars = new File("data" + File.separator + "jars");
    public static Toml env = new Toml();
    public static ExecutorService servicePool = Executors.newCachedThreadPool();
    public static ExecutorService taskPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
    public static Map<String, Object> envMap;
    public static Config cfg = Config.initConfig();
    public static final Map<String, String> publicVars = new ConcurrentHashMap<>();
    private static String command;
    public static final List<AnnotationConfigApplicationContext> contexts = new ArrayList<>();

    public static void main(String[] args) {
        LOGGER.info("Basin started.");
        Thread init = new Thread(() -> {
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
            contexts.forEach(i -> {

            });
        });
        taskPool.submit(init);
        new Thread(() -> {
            Conversation conversation = new Conversation();
            regCommands();
            if (cfg.enableRemote && !cfg.accessToken.isBlank()) {
                try {
                    new Server(cfg.accessToken, cfg.remotePort, new Conversation()).start();
                } catch (Exception e) {
                    LOGGER.error(e.toString());
                }
            }
            if (!cfg.startCommand.isBlank()) CONSOLE_CONVERSATION.parse(cfg.startCommand);
            Scanner scanner = new Scanner(System.in);
            while (true) {
                command = scanner.nextLine();
                taskPool.submit(new Thread(() -> {
                    try {
                        conversation.parse(command);
                    } catch (Exception e) {
                        LOGGER.error(e.toString());
                    }
                }));
            }
        }).start();
        System.out.println(Basin.getBasin().basin);
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

    @SuppressWarnings({"DataFlowIssue", "ConstantValue"})
    public static boolean publicRunCommand(@NotNull List<String> alc) {
        for (String cmd : alc) {
            ArrayList<String> args = new ArrayList<>();
            for (String i : List.of(StringUtils.split(cmd.strip(), ' '))) {
                if (!i.startsWith("$")) {
                    args.add(i);
                } else {
                    String string;
                    if (publicVars != null) {
                        string = publicVars.get(i.replaceFirst("\\$", ""));
                        args.add(string);
                    }
                }
            }
            String cmdName = args.get(0);
            if (cmdName.matches(".*=.*")) {
                String[] var = StringUtils.split(cmdName, "=");
                publicVars.put(var[0].strip(), var[1].strip());
                return true;
            }
            args.remove(cmdName);
            Command run = commands.get(cmdName.toLowerCase().strip());
            LOGGER.info("Starting: " + cmd);
            if (!(run == null)) {
                try {
                    run.run(args);
                    return true;
                } catch (IndexOutOfBoundsException e) {
                    LOGGER.error("Bad arg input.");
                } catch (Exception e) {
                    LOGGER.error(e.toString());
                }
            } else LOGGER.error("Unknown command: " + cmdName);
        }
        return false;
    }

    public static boolean publicRunCommand(@NotNull String ac) {
        if (ac.isBlank() || ac.startsWith("#")) return true;
        ArrayList<String> alc = new ArrayList<>(List.of(StringUtils.split(ac.strip().replace("/", ""), '&')));
        AtomicBoolean p = new AtomicBoolean(true);
        alc.forEach((cmd) -> {
            if (!publicRunCommand(alc)) p.set(false);
        });
        return p.get();
    }

    public static void regCommands() {
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
        register(new ShellCommand());
    }

    public static void register(Command cmd) {
        commands.put(cmd.Name(), cmd);
    }
}
